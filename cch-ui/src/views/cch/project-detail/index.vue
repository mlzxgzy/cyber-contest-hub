<script lang="ts" setup>
import {computed, h, onMounted, ref} from 'vue';
import type {DataTableColumns} from 'naive-ui';
import {useRoute, useRouter} from 'vue-router';
import dayjs from 'dayjs';
import {jsonClone} from '@sa/utils';
import {fetchGetProjectDetail, fetchJoinProjectByInvite, fetchUpdateProject} from '@/service/api/cch/project';
import {useAuthStore} from '@/store/modules/auth';
import ProjectMemberManage from './modules/project-member-manage.vue';
import ProjectChallengeManage from './modules/project-challenge-manage.vue';
import ProjectFileManage from './modules/project-file-manage.vue';
import InlineEditRow from './modules/inline-edit-row.vue';
import {$t} from '@/locales';
import {useTabStore} from '@/store/modules/tab';

defineOptions({
  name: 'ProjectDetail'
});

const route = useRoute();
const router = useRouter();
const tabStore = useTabStore();
const authStore = useAuthStore();

const loading = ref(false);
const project = ref<Api.Cch.Project | null>(null);
const isProjectAdmin = ref(false);
const isProjectMember = ref(false);
const activeTab = ref('basic');

const currentUserId = computed(() => authStore.userInfo.user?.userId);

// 系统超级管理员：与租户管理页保持一致，userId === 1 视为超管
const isSuperAdmin = computed(() => currentUserId.value === 1);

// 当前用户在项目中的权限类型（admin/view_all/view_own），超管视为 admin
const currentPermissionType = computed<'admin' | 'view_all' | 'view_own' | null>(() => {
  if (isSuperAdmin.value) {
    return 'admin';
  }
  if (!project.value?.members || !currentUserId.value) {
    return null;
  }
  const currentUserMember = project.value.members.find(m => m.userId === currentUserId.value);
  return (currentUserMember?.permissionType as 'admin' | 'view_all' | 'view_own') || null;
});

// 页面与各模块可见性
const canViewProject = computed(() => isProjectMember.value || isSuperAdmin.value);
const canViewMembersTab = computed(() => canViewProject.value);
const canViewChallengesTab = computed(() => canViewProject.value);
const canViewFilesTab = computed(
  () => project.value?.projectType === 'contest' && (isProjectAdmin.value || isSuperAdmin.value)
);

const isContest = computed(() => project.value?.projectType === 'contest');
const isAuthoring = computed(() => project.value?.projectType === 'authoring');
const isExternalAuthoring = computed(
  () => isAuthoring.value && project.value?.authoringMeta?.authorSource === 'external'
);

const projectTypeLabelMap: Record<string, string> = {
  normal: '普通项目',
  contest: '竞赛项目',
  authoring: '出题项目'
};

const authorSourceLabelMap: Record<string, string> = {
  self: '自己出',
  external: '外采'
};

const authorSourceOptions = [
  {label: '自己出', value: 'self'},
  {label: '外采', value: 'external'}
];

// 基本信息行内编辑权限：项目管理员或超管
const canEditBasicInfo = computed(() => isProjectAdmin.value || isSuperAdmin.value);

// 基本信息视图模式：view 展示视角 / edit 编辑视角（仅管理员可切换）
const basicViewMode = ref<'view' | 'edit'>('view');
// 是否处于展示视角（非管理员恒为展示视角）
const isBasicViewMode = computed(() => !canEditBasicInfo.value || basicViewMode.value === 'view');

const savingBasic = ref(false);

// ===== 项目负责人历史（保存在浏览器 localStorage，填写时弹出推荐） =====
const LEADER_HISTORY_KEY = 'cch_project_leader_history';
const LEADER_HISTORY_MAX = 20;
const leaderHistory = ref<string[]>(readLeaderHistory());

/** 从 localStorage 读取负责人历史 */
function readLeaderHistory(): string[] {
  try {
    const raw = localStorage.getItem(LEADER_HISTORY_KEY);
    const parsed = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? parsed.filter((v): v is string => typeof v === 'string' && v.trim() !== '') : [];
  } catch {
    return [];
  }
}

/** 将负责人姓名记入本地历史（去重、最新在前、限量保存） */
function rememberLeaderHistory(name: string) {
  const trimmed = name.trim();
  if (!trimmed) return;
  const next = [trimmed, ...leaderHistory.value.filter(item => item !== trimmed)].slice(0, LEADER_HISTORY_MAX);
  leaderHistory.value = next;
  try {
    localStorage.setItem(LEADER_HISTORY_KEY, JSON.stringify(next));
  } catch {
    // localStorage 不可用时静默忽略
  }
}

// 阶段/平台编辑态（管理员在详情页直接增删改）
const editableStages = ref<Api.Cch.ContestStage[]>([]);
const editablePlatforms = ref<Api.Cch.ContestPlatform[]>([]);

/** 将 meta 中的阶段/平台同步到编辑态 */
function syncStagePlatformEditable() {
  const stages: Api.Cch.ContestStage[] = jsonClone(project.value?.meta?.stages) || [];
  // 将空字符串开始时间归一化为 null，避免 NDatePicker 解析空串报 Invalid time value
  stages.forEach(stage => {
    if (stage.startTime === '') {
      stage.startTime = null;
    }
  });
  editableStages.value = stages;
  editablePlatforms.value = jsonClone(project.value?.meta?.platforms) || [];
}

/** 新增一个阶段 */
function addStage() {
  editableStages.value.push({
    stageName: '',
    startTime: null,
    duration: null,
    challengeRequirement: ''
  });
}

/** 删除指定阶段 */
function removeStage(index: number) {
  editableStages.value.splice(index, 1);
}

/** 新增一个平台 */
function addPlatform() {
  editablePlatforms.value.push({
    platformName: '',
    platformUrl: ''
  });
}

/** 删除指定平台 */
function removePlatform(index: number) {
  editablePlatforms.value.splice(index, 1);
}

/** 保存阶段与平台，成功后自动回到展示视角 */
async function saveStagesPlatforms() {
  const ok = await saveBasicField({
    meta: {
      stages: jsonClone(editableStages.value),
      platforms: jsonClone(editablePlatforms.value)
    }
  });
  if (ok) {
    basicViewMode.value = 'view';
  }
}

/** 展示视角：竞赛阶段只读数据 */
const viewStages = computed<Api.Cch.ContestStage[]>(() => project.value?.meta?.stages || []);

/** 展示视角：竞赛平台只读数据 */
const viewPlatforms = computed<Api.Cch.ContestPlatform[]>(() => project.value?.meta?.platforms || []);

/** 展示视角：竞赛阶段汇总表格列 */
const stageViewColumns: DataTableColumns<Api.Cch.ContestStage> = [
  {key: 'index', title: '#', width: 48, align: 'center', render: (_row, index) => index + 1},
  {key: 'stageName', title: '阶段名称', minWidth: 100},
  {key: 'startTime', title: '开始时间', minWidth: 140},
  {
    key: 'duration',
    title: '阶段时长（分钟）',
    width: 130,
    align: 'center',
    render: row => (row.duration ? String(row.duration) : '-')
  },
  {key: 'endTime', title: '结束时间', minWidth: 140, render: row => getStageEndTime(row) || '-'},
  {
    key: 'challengeRequirement',
    title: '本阶段赛题需求',
    minWidth: 200,
    render: row => h('span', {class: 'whitespace-pre-wrap'}, row.challengeRequirement || '-')
  }
];

/** 展示视角：竞赛平台汇总表格列 */
const platformViewColumns: DataTableColumns<Api.Cch.ContestPlatform> = [
  {key: 'index', title: '#', width: 48, align: 'center', render: (_row, index) => index + 1},
  {key: 'platformName', title: '平台名称', minWidth: 140},
  {
    key: 'platformUrl',
    title: '平台地址',
    minWidth: 220,
    render: row =>
      row.platformUrl
        ? h('a', {href: row.platformUrl, target: '_blank', rel: 'noopener noreferrer', class: 'text-primary'}, row.platformUrl)
        : '-'
  }
];

/** 进入基本信息编辑视角（以服务端数据重新初始化阶段/平台编辑态） */
function enterBasicEdit() {
  syncStagePlatformEditable();
  basicViewMode.value = 'edit';
}

/** 根据开始时间与阶段时长（分钟）自动计算结束时间，未填时长时不显示 */
function getStageEndTime(stage: Api.Cch.ContestStage): string {
  if (!stage.startTime || !stage.duration || stage.duration <= 0) {
    return '';
  }
  const start = dayjs(stage.startTime);
  if (!start.isValid()) {
    return '';
  }
  return start.add(stage.duration, 'minute').format('YYYY-MM-DD HH:mm');
}

/**
 * 保存基本信息字段（乐观更新，失败回滚）
 *
 * 竞赛项目中竞赛名称/赛事备注与项目名称/备注保持同步
 *
 * @returns 是否保存成功
 */
async function saveBasicField(patch: {
  name?: string;
  leader?: string;
  remark?: string;
  authoringMeta?: Partial<Api.Cch.AuthoringMeta>;
  meta?: Partial<Api.Cch.ContestMeta>;
}): Promise<boolean> {
  if (!project.value || savingBasic.value) return false;
  const isContestProject = project.value.projectType === 'contest';

  const mergedMeta: Api.Cch.ContestMeta = {
    contestName: project.value.meta?.contestName ?? project.value.name ?? '',
    contestRemark: project.value.meta?.contestRemark ?? project.value.remark ?? '',
    startTime: project.value.meta?.startTime ?? '',
    endTime: project.value.meta?.endTime ?? '',
    challengeRequirement: project.value.meta?.challengeRequirement ?? '',
    stages: jsonClone(project.value.meta?.stages) || [],
    platforms: jsonClone(project.value.meta?.platforms) || []
  };
  if (patch.name !== undefined) {
    mergedMeta.contestName = patch.name;
  }
  if (patch.remark !== undefined) {
    mergedMeta.contestRemark = patch.remark;
  }
  if (patch.meta) {
    Object.assign(mergedMeta, patch.meta);
  }

  const prevProject = jsonClone(project.value);

  // 乐观更新本地状态
  if (patch.name !== undefined) project.value.name = patch.name;
  if (patch.leader !== undefined) project.value.leader = patch.leader;
  if (patch.remark !== undefined) project.value.remark = patch.remark;
  if (patch.authoringMeta !== undefined) {
    project.value.authoringMeta = {...(project.value.authoringMeta || {}), ...patch.authoringMeta};
  }
  if (isContestProject) project.value.meta = mergedMeta;

  savingBasic.value = true;
  const {error} = await fetchUpdateProject({
    id: project.value.id,
    projectType: project.value.projectType,
    name: project.value.name,
    leader: project.value.leader ?? '',
    remark: project.value.remark ?? '',
    authoringMeta:
      project.value.projectType === 'authoring'
        ? {...(project.value.authoringMeta || {}), ...(patch.authoringMeta || {})}
        : undefined,
    meta: isContestProject ? mergedMeta : undefined
  });
  savingBasic.value = false;

  if (error) {
    project.value = prevProject;
    window.$message?.error(error.message || '保存失败');
    return false;
  }

  if (patch.name !== undefined) {
    tabStore.setTabLabel(`项目详情-${patch.name}`);
  }
  if (patch.leader !== undefined) {
    rememberLeaderHistory(patch.leader);
  }
  window.$message?.success('保存成功');
  return true;
}

async function loadProjectDetail() {
  const projectId = route.params.id as string;
  if (!projectId) {
    window.$message?.error('项目ID不能为空');
    router.back();
    return;
  }

  loading.value = true;

  // 如果存在邀请Code，则先尝试通过邀请加入项目
  const inviteCode = route.query.invite as string | undefined;
  if (inviteCode) {
    const {error: joinError} = await fetchJoinProjectByInvite(projectId, inviteCode);
    if (joinError) {
      // 加入失败仅提示，不阻断项目详情加载
      window.$message?.error(joinError.message || '通过邀请加入项目失败');
    } else {
      window.$message?.success('已通过邀请加入项目');
      // 邀请加入成功后，移除 URL 中的 invite 参数，避免后续重复触发加入逻辑
      const {invite, ...restQuery} = route.query as Record<string, any>;
      router.replace({query: restQuery});
    }
  }

  // 直接使用字符串类型的 ID，避免大整数精度丢失
  const {data, error} = await fetchGetProjectDetail(projectId);

  if (error || !data) {
    window.$message?.error('加载项目详情失败');
    router.back();
    return;
  }

  project.value = data;
  // 同步阶段/平台到编辑态
  syncStagePlatformEditable();

  // 更新当前标签页名称为：项目详情-项目名称
  if (data.name) {
    tabStore.setTabLabel(`项目详情-${data.name}`);
  }

  // 检查当前用户是否为项目成员及项目管理员
  if (data.members && currentUserId.value) {
    const currentUserMember = data.members.find(m => m.userId === currentUserId.value);
    isProjectMember.value = !!currentUserMember;
    isProjectAdmin.value = currentUserMember?.permissionType === 'admin';
  } else {
    isProjectMember.value = false;
    isProjectAdmin.value = false;
  }

  loading.value = false;
}

function handleRefresh() {
  loadProjectDetail();
}

onMounted(() => {
  loadProjectDetail();
});

</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-auto">
    <NSpin :show="loading">
      <NCard v-if="project" :bordered="false" class="card-wrapper sm:flex-1-hidden" size="small">
        <template #header>
          <div class="flex items-center justify-between">
            <span>项目详情</span>
            <NButton text @click="router.back()">
              <template #icon>
                <icon-ic-round-arrow-back class="text-icon"/>
              </template>
              返回
            </NButton>
          </div>
        </template>

        <NTabs v-model:value="activeTab" type="line" animated>
          <NTabPane name="basic" tab="基本信息">
            <NCard :bordered="false" size="small">
              <template v-if="canViewProject">
                <!-- 视角切换：仅项目管理员/超管可进入编辑视角 -->
                <div v-if="canEditBasicInfo" class="mb-12px flex justify-end">
                  <NButton v-if="isBasicViewMode" size="small" type="primary" ghost @click="enterBasicEdit">
                    <template #icon>
                      <icon-ic-round-edit class="text-icon"/>
                    </template>
                    编辑
                  </NButton>
                  <NButton v-else size="small" @click="basicViewMode = 'view'">退出编辑</NButton>
                </div>

                <!-- 展示视角：仅展示数据与内容 -->
                <div v-if="isBasicViewMode" class="flex flex-col gap-16px">
                  <NDescriptions label-placement="left" bordered :column="2" size="small">
                    <NDescriptionsItem label="项目类型">
                      {{ projectTypeLabelMap[project.projectType] || project.projectType }}
                    </NDescriptionsItem>
                    <NDescriptionsItem v-if="isAuthoring" label="出题方式">
                      {{ authorSourceLabelMap[project.authoringMeta?.authorSource || ''] || '-' }}
                    </NDescriptionsItem>
                    <NDescriptionsItem v-if="isExternalAuthoring" label="外采单位">
                      {{ project.authoringMeta?.externalUnit || '-' }}
                    </NDescriptionsItem>
                    <NDescriptionsItem :label="isContest ? '竞赛名称' : '项目名称'">
                      {{ isContest ? project.meta?.contestName || project.name : project.name }}
                    </NDescriptionsItem>
                    <NDescriptionsItem label="项目负责人">
                      {{ project.leader || '-' }}
                    </NDescriptionsItem>
                    <NDescriptionsItem :label="isContest ? '赛事备注' : '备注'" :span="2">
                      <span class="whitespace-pre-wrap">
                        {{ isContest ? project.meta?.contestRemark ?? project.remark ?? '-' : project.remark ?? '-' }}
                      </span>
                    </NDescriptionsItem>
                    <template v-if="isContest">
                      <NDescriptionsItem label="开始时间">
                        {{ project.meta?.startTime || '-' }}
                      </NDescriptionsItem>
                      <NDescriptionsItem label="结束时间">
                        {{ project.meta?.endTime || '-' }}
                      </NDescriptionsItem>
                      <NDescriptionsItem label="题目需求" :span="2">
                        <span class="whitespace-pre-wrap">{{ project.meta?.challengeRequirement || '-' }}</span>
                      </NDescriptionsItem>
                    </template>
                    <NDescriptionsItem label="创建时间">
                      {{ project.createTime || '-' }}
                    </NDescriptionsItem>
                    <NDescriptionsItem label="更新时间">
                      {{ project.updateTime || '-' }}
                    </NDescriptionsItem>
                  </NDescriptions>

                  <template v-if="isContest">
                    <div class="flex flex-col gap-8px">
                      <NDivider title-placement="left">竞赛阶段</NDivider>
                      <NDataTable
                        v-if="viewStages.length"
                        size="small"
                        :columns="stageViewColumns"
                        :data="viewStages"
                      />
                      <NEmpty v-else description="暂无竞赛阶段" size="small" />
                    </div>
                    <div class="flex flex-col gap-8px">
                      <NDivider title-placement="left">竞赛平台</NDivider>
                      <NDataTable
                        v-if="viewPlatforms.length"
                        size="small"
                        :columns="platformViewColumns"
                        :data="viewPlatforms"
                      />
                      <NEmpty v-else description="暂无竞赛平台" size="small" />
                    </div>
                  </template>
                </div>

                <!-- 编辑视角：与管理员原有行内编辑保持一致 -->
                <div v-else class="flex flex-col">
                  <InlineEditRow
                    label="项目类型"
                    :value="projectTypeLabelMap[project.projectType] || project.projectType"
                  />
                  <InlineEditRow
                    v-if="isAuthoring"
                    label="出题方式"
                    :value="authorSourceLabelMap[project.authoringMeta?.authorSource || ''] || ''"
                    type="select"
                    :options="authorSourceOptions"
                    :editable="canEditBasicInfo"
                    :saving="savingBasic"
                    placeholder="点击选择出题方式"
                    @save="(v: string) => saveBasicField({authoringMeta: {authorSource: v as 'self' | 'external'}})"
                  />
                  <InlineEditRow
                    v-if="isExternalAuthoring"
                    label="外采单位"
                    :value="project.authoringMeta?.externalUnit ?? ''"
                    :editable="canEditBasicInfo"
                    :saving="savingBasic"
                    placeholder="点击填写外采单位"
                    @save="(v: string) => saveBasicField({authoringMeta: {externalUnit: v}})"
                  />
                  <InlineEditRow
                    :label="isContest ? '竞赛名称' : '项目名称'"
                    :value="isContest ? project.meta?.contestName || project.name : project.name"
                    :editable="canEditBasicInfo"
                    :saving="savingBasic"
                    placeholder="点击填写"
                    @save="(v: string) => saveBasicField({name: v})"
                  />
                  <InlineEditRow
                    label="项目负责人"
                    :value="project.leader ?? ''"
                    :editable="canEditBasicInfo"
                    :saving="savingBasic"
                    placeholder="点击填写负责人姓名"
                    :suggestions="leaderHistory"
                    @save="(v: string) => saveBasicField({leader: v})"
                  />
                  <InlineEditRow
                    :label="isContest ? '赛事备注' : '备注'"
                    :value="isContest ? project.meta?.contestRemark ?? project.remark ?? '' : project.remark ?? ''"
                    :editable="canEditBasicInfo"
                    :saving="savingBasic"
                    placeholder="点击填写"
                    @save="(v: string) => saveBasicField({remark: v})"
                  />
                  <template v-if="isContest">
                    <InlineEditRow
                      label="开始时间"
                      :value="project.meta?.startTime ?? ''"
                      type="date"
                      :editable="canEditBasicInfo"
                      :saving="savingBasic"
                      placeholder="点击选择"
                      @save="(v: string) => saveBasicField({meta: {startTime: v}})"
                    />
                    <InlineEditRow
                      label="结束时间"
                      :value="project.meta?.endTime ?? ''"
                      type="date"
                      :editable="canEditBasicInfo"
                      :saving="savingBasic"
                      placeholder="点击选择"
                      @save="(v: string) => saveBasicField({meta: {endTime: v}})"
                    />
                    <InlineEditRow
                      label="题目需求"
                      :value="project.meta?.challengeRequirement ?? ''"
                      type="textarea"
                      :editable="canEditBasicInfo"
                      :saving="savingBasic"
                      placeholder="点击填写题目需求说明"
                      @save="(v: string) => saveBasicField({meta: {challengeRequirement: v}})"
                    />
                  </template>

                  <template v-if="isContest">
                    <!-- 竞赛阶段编辑块 -->
                    <NDivider title-placement="left">竞赛阶段</NDivider>
                    <template v-if="canEditBasicInfo">
                      <div v-for="(stage, index) in editableStages" :key="index" class="edit-block">
                        <div class="edit-row">
                          <div class="edit-field">
                            <label class="edit-label">阶段{{ index + 1 }}名称</label>
                            <NInput v-model:value="stage.stageName" placeholder="如：初赛 / 决赛 / 选拔赛"/>
                          </div>
                          <div class="edit-field">
                            <label class="edit-label">开始时间</label>
                            <NDatePicker
                              v-model:formatted-value="stage.startTime"
                              type="datetime"
                              value-format="yyyy-MM-dd HH:mm"
                              clearable
                              placeholder="请选择开始时间"
                              class="w-full"
                            />
                          </div>
                          <div class="edit-field">
                            <label class="edit-label">阶段时长（分钟）</label>
                            <NInputNumber
                              v-model:value="stage.duration"
                              :min="0"
                              :step="10"
                              clearable
                              placeholder="如：120"
                              class="w-full"
                            />
                          </div>
                        </div>
                        <div class="edit-field mt-8px">
                          <label class="edit-label">结束时间</label>
                          <div class="edit-endtime">
                            {{ getStageEndTime(stage) || '填写开始时间与时长后自动计算' }}
                          </div>
                        </div>
                        <div class="edit-field mt-8px">
                          <label class="edit-label">本阶段赛题需求</label>
                          <NInput
                            v-model:value="stage.challengeRequirement"
                            type="textarea"
                            :rows="2"
                            placeholder="请输入本阶段赛题需求"
                          />
                        </div>
                        <div class="edit-remove">
                          <NButton native-type="button" size="small" type="error" quaternary @click="removeStage(index)">
                            删除此阶段
                          </NButton>
                        </div>
                      </div>
                      <NButton native-type="button" dashed block class="mb-12px" @click="addStage">
                        <template #icon><NIcon><i class="i-carbon-add"/></NIcon></template>
                        添加阶段
                      </NButton>
                    </template>
                    <NEmpty
                      v-if="!editableStages.length"
                      description="暂无竞赛阶段，点击上方「添加阶段」按钮新增"
                      size="small"
                    />

                    <!-- 竞赛平台编辑块 -->
                    <NDivider title-placement="left">竞赛平台</NDivider>
                    <template v-if="canEditBasicInfo">
                      <div v-for="(platform, index) in editablePlatforms" :key="index" class="edit-block">
                        <div class="edit-row">
                          <div class="edit-field">
                            <label class="edit-label">平台名称</label>
                            <NInput v-model:value="platform.platformName" placeholder="请输入平台名称"/>
                          </div>
                          <div class="edit-field">
                            <label class="edit-label">平台地址</label>
                            <NInput v-model:value="platform.platformUrl" placeholder="请输入平台地址（URL）"/>
                          </div>
                        </div>
                        <div class="edit-remove">
                          <NButton native-type="button" size="small" type="error" quaternary @click="removePlatform(index)">
                            删除此平台
                          </NButton>
                        </div>
                      </div>
                      <NButton native-type="button" dashed block class="mb-12px" @click="addPlatform">
                        <template #icon><NIcon><i class="i-carbon-add"/></NIcon></template>
                        添加平台
                      </NButton>
                    </template>
                    <NEmpty
                      v-if="!editablePlatforms.length"
                      description="暂无竞赛平台，点击上方「添加平台」按钮新增"
                      size="small"
                    />

                    <!-- 管理员：统一保存阶段/平台 -->
                    <div v-if="canEditBasicInfo" class="flex justify-end mt-12px">
                      <NButton native-type="button" type="primary" :loading="savingBasic" @click="saveStagesPlatforms">
                        保存阶段与平台
                      </NButton>
                    </div>
                  </template>
                  <InlineEditRow label="创建时间" :value="project.createTime ?? ''" />
                  <InlineEditRow label="更新时间" :value="project.updateTime ?? ''" />
                </div>
              </template>
              <template v-else>
                <NResult
                  status="warning"
                  title="暂无访问权限"
                  description="仅项目成员或系统超级管理员可查看项目详情"
                />
              </template>
            </NCard>
          </NTabPane>

          <NTabPane v-if="canViewMembersTab" name="members" tab="成员管理">
            <ProjectMemberManage
              :project-id="project.id"
              :is-project-admin="isProjectAdmin"
              :can-manage-members="isProjectAdmin || isSuperAdmin"
              @refresh="handleRefresh"
            />
          </NTabPane>

          <NTabPane v-if="canViewChallengesTab" name="challenges" tab="题目管理">
            <ProjectChallengeManage
              :project-id="project.id"
              :is-project-admin="isProjectAdmin"
              :current-permission-type="currentPermissionType"
              :is-super-admin="isSuperAdmin"
              @refresh="handleRefresh"
            />
          </NTabPane>

          <NTabPane v-if="canViewFilesTab" name="files" tab="文件管理">
            <ProjectFileManage
              :project-id="project.id"
              :project-type="project.projectType"
              :is-project-admin="isProjectAdmin"
              :is-super-admin="isSuperAdmin"
              :files="project.contestFiles || []"
              @refresh="handleRefresh"
            />
          </NTabPane>
        </NTabs>
      </NCard>
    </NSpin>
  </div>
</template>

<style scoped>
.edit-block {
  border: 1px dashed var(--n-border-color);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}

.edit-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.edit-field {
  flex: 1 1 200px;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.edit-label {
  font-size: 12px;
  color: var(--n-text-color-3, #666);
  margin-bottom: 4px;
}

.edit-remove {
  display: flex;
  justify-content: flex-end;
  margin-top: 4px;
}

.edit-endtime {
  padding: 6px 11px;
  border: 1px solid var(--n-border-color);
  border-radius: 4px;
  background: var(--n-color, transparent);
  color: var(--n-text-color-3, #666);
  min-height: 34px;
  display: flex;
  align-items: center;
}
</style>
