<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {fetchGetProjectDetail, fetchJoinProjectByInvite} from '@/service/api/cch/project';
import {useAuth} from '@/hooks/business/auth';
import {useAuthStore} from '@/store/modules/auth';
import ProjectMemberManage from './modules/project-member-manage.vue';
import ProjectChallengeManage from './modules/project-challenge-manage.vue';
import ProjectFileManage from './modules/project-file-manage.vue';
import {$t} from '@/locales';
import {useTabStore} from '@/store/modules/tab';

defineOptions({
  name: 'ProjectDetail'
});

const route = useRoute();
const router = useRouter();
const tabStore = useTabStore();
const {hasAuth} = useAuth();
const authStore = useAuthStore();

const loading = ref(false);
const project = ref<Api.Cch.Project | null>(null);
const isProjectAdmin = ref(false);
const isProjectMember = ref(false);
const activeTab = ref('basic');

const currentUserId = computed(() => authStore.userInfo.user?.userId);

const tabs = computed(() => {
  const tabList: Array<{ name: string; label: string }> = [
    {name: 'basic', label: '基本信息'},
    {name: 'members', label: '成员管理'},
    {name: 'challenges', label: '题目管理'}
  ];

  // 文件管理：仅竞赛项目且需要项目管理员权限
  if (project.value?.projectType === 'contest' && isProjectAdmin.value && hasAuth('cch:project:file')) {
    tabList.push({name: 'files', label: '文件管理'});
  }

  return tabList;
});

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

  // 更新当前标签页名称为：项目详情-项目名称
  if (data.name) {
    tabStore.setTabLabel(`项目详情-${data.name}`);
  }

  // 检查当前用户是否为项目成员及项目管理员
  if (data.members && currentUserId.value) {
    const currentUserMember = data.members.find(m => m.userId === currentUserId.value);
    isProjectMember.value = !!currentUserMember;
    isProjectAdmin.value = currentUserMember?.permissionType === 'admin';
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
              <!-- 调整为两列布局，长文本字段跨两列展示 -->
              <NDescriptions :column="2" bordered>
                <NDescriptionsItem label="项目类型">
                  {{ project.projectType === 'contest' ? '竞赛项目' : '普通项目' }}
                </NDescriptionsItem>
                <NDescriptionsItem label="项目名称">
                  {{ project.name }}
                </NDescriptionsItem>
                <NDescriptionsItem label="备注" :span="2">
                  {{ project.remark || '-' }}
                </NDescriptionsItem>
                <NDescriptionsItem v-if="project.projectType === 'contest' && project.meta" label="竞赛名称">
                  {{ project.meta.contestName || '-' }}
                </NDescriptionsItem>
                <NDescriptionsItem v-if="project.projectType === 'contest' && project.meta" label="赛事备注" :span="2">
                  {{ project.meta.contestRemark || '-' }}
                </NDescriptionsItem>
                <NDescriptionsItem v-if="project.projectType === 'contest' && project.meta" label="开始时间">
                  {{ project.meta.startTime || '-' }}
                </NDescriptionsItem>
                <NDescriptionsItem v-if="project.projectType === 'contest' && project.meta" label="结束时间">
                  {{ project.meta.endTime || '-' }}
                </NDescriptionsItem>
                <NDescriptionsItem label="创建时间">
                  {{ project.createTime }}
                </NDescriptionsItem>
                <NDescriptionsItem label="更新时间">
                  {{ project.updateTime }}
                </NDescriptionsItem>
              </NDescriptions>
            </NCard>
          </NTabPane>

          <NTabPane v-if="tabs.some(t => t.name === 'members')" name="members" tab="成员管理">
            <ProjectMemberManage
              :project-id="project.id"
              :is-project-admin="isProjectAdmin"
              @refresh="handleRefresh"
            />
          </NTabPane>

          <NTabPane v-if="tabs.some(t => t.name === 'challenges')" name="challenges" tab="题目管理">
            <ProjectChallengeManage
              :project-id="project.id"
              :is-project-admin="isProjectAdmin"
              @refresh="handleRefresh"
            />
          </NTabPane>

          <NTabPane v-if="tabs.some(t => t.name === 'files')" name="files" tab="文件管理">
            <ProjectFileManage
              :project-id="project.id"
              :project-type="project.projectType"
              :is-project-admin="isProjectAdmin"
              :files="project.contestFiles || []"
              @refresh="handleRefresh"
            />
          </NTabPane>
        </NTabs>
      </NCard>
    </NSpin>
  </div>
</template>

<style scoped></style>
