<script lang="ts" setup>
import {computed, h, onMounted, ref, watch} from 'vue';
import type {DataTableColumns} from 'naive-ui';
import {NButton, NCard, NDataTable, NSelect, NSpace, NTag, useDialog} from 'naive-ui';
import {fetchGetChallengeProjects} from '@/service/api/cch/challenge';
import {fetchGetProjectList, fetchImportProjectChallenges, fetchRemoveProjectChallenges} from '@/service/api/cch/project';
import {useAuth} from '@/hooks/business/auth';

defineOptions({
  name: 'ChallengeProjectAttach'
});

interface Props {
  challengeId: CommonType.IdType | null;
  /** 当前发版版本ID（附加时使用的版本） */
  latestVersionId: CommonType.IdType | null;
}

const props = defineProps<Props>();

const dialog = useDialog();
const {hasAuth} = useAuth();

// 是否有项目编辑权限（附加/移除需要 cch:project:edit 菜单权限 + 目标项目管理员权限）
const canOperate = computed(() => hasAuth('cch:project:edit'));

const loading = ref(false);
const attachedProjects = ref<Api.Cch.ProjectChallenge[]>([]);

// 可附加的项目列表（与项目列表页同一 API）
const projectOptions = ref<Array<{label: string; value: CommonType.IdType}>>([]);
const projectsLoading = ref(false);

// 已附加当前发版版本的项目ID集合（用于过滤：同一项目已附加旧版本时仍可附加新版本）
const attachedCurrentVersionProjectIds = computed(() => {
  const latest = props.latestVersionId;
  if (!latest) return new Set<CommonType.IdType>();
  return new Set(
    attachedProjects.value
      .filter(item => item.versionId === latest)
      .map(item => item.projectId)
  );
});

// 附加选择
const selectedProjectIds = ref<CommonType.IdType[]>([]);
const attaching = ref(false);

// 已附加列表表格列
const columns = computed<DataTableColumns<Api.Cch.ProjectChallenge>>(() => [
  {
    key: 'projectName',
    title: '项目名称',
    align: 'center',
    minWidth: 160
  },
  {
    key: 'versionTag',
    title: '版本号',
    align: 'center',
    minWidth: 120
  },
  {
    key: 'createTime',
    title: '附加时间',
    align: 'center',
    minWidth: 160
  },
  {
    key: 'createByName',
    title: '附加人',
    align: 'center',
    minWidth: 120
  },
  {
    key: 'operate',
    title: '操作',
    align: 'center',
    width: 100,
    render: row => {
      if (!canOperate.value) {
        return null;
      }
      return h(
        NButton,
        {
          text: true,
          type: 'error',
          size: 'small',
          onClick: () => handleRemove(row)
        },
        {default: () => '移除'}
      );
    }
  }
]);

// 附加下拉选项：过滤掉已附加当前发版版本的项目（已附加旧版本的项目仍可附加新版本）
const availableProjectOptions = computed(() => {
  return projectOptions.value.filter(option => !attachedCurrentVersionProjectIds.value.has(option.value));
});

// 加载已附加项目列表
async function loadAttachedProjects() {
  if (!props.challengeId) return;
  loading.value = true;
  try {
    const {data, error} = await fetchGetChallengeProjects(props.challengeId);
    if (error) {
      window.$message?.error(`获取已附加项目失败: ${error}`);
      return;
    }
    attachedProjects.value = data || [];
  } finally {
    loading.value = false;
  }
}

// 加载可附加项目列表（分页拉取，默认取前100条）
async function loadProjects() {
  projectsLoading.value = true;
  try {
    const {data, error} = await fetchGetProjectList({
      pageNum: 1,
      pageSize: 100,
      projectType: null,
      params: {}
    });
    if (error) {
      window.$message?.error(`获取项目列表失败: ${error}`);
      return;
    }
    projectOptions.value = (data?.rows || []).map(project => ({
      label: project.name,
      value: project.id
    }));
  } finally {
    projectsLoading.value = false;
  }
}

// 附加当前发版版本到所选项目
async function handleAttach() {
  if (!props.latestVersionId) {
    window.$message?.warning('当前题目尚未发版，无法附加到项目');
    return;
  }
  if (selectedProjectIds.value.length === 0) {
    window.$message?.warning('请先选择要附加到的项目');
    return;
  }

  attaching.value = true;
  let successCount = 0;
  let failCount = 0;
  try {
    for (const projectId of selectedProjectIds.value) {
      try {
        const {error} = await fetchImportProjectChallenges(projectId, [{versionId: props.latestVersionId}]);
        if (error) {
          failCount += 1;
          console.warn(`附加到项目 ${projectId} 失败:`, error);
        } else {
          successCount += 1;
        }
      } catch (err) {
        failCount += 1;
        console.warn(`附加到项目 ${projectId} 异常:`, err);
      }
    }
  } finally {
    if (successCount > 0) {
      window.$message?.success(`成功附加到 ${successCount} 个项目`);
    }
    if (failCount > 0) {
      window.$message?.error(`${failCount} 个项目附加失败（可能需要项目管理员权限）`);
    }
    selectedProjectIds.value = [];
    attaching.value = false;
    await loadAttachedProjects();
  }
}

// 移除已附加的项目
function handleRemove(row: Api.Cch.ProjectChallenge) {
  dialog.warning({
    title: '确认移除',
    content: `确定要将该项目下的「${row.versionTag || row.versionId}」版本从项目「${row.projectName || row.projectId}」中移除吗？`,
    positiveText: '移除',
    negativeText: '取消',
    onPositiveClick: async () => {
      const {data, error} = await fetchRemoveProjectChallenges(row.projectId, [row.id]);
      if (error) {
        window.$message?.error(`移除失败: ${error}`);
        return;
      }
      if (data) {
        window.$message?.success('移除成功');
      } else {
        window.$message?.warning('未找到可移除的关联记录');
      }
      await loadAttachedProjects();
    }
  });
}

watch(
  () => props.challengeId,
  val => {
    if (val) {
      loadAttachedProjects();
    }
  },
  {immediate: true}
);

onMounted(() => {
  loadProjects();
});
</script>

<template>
  <div class="attach-container">
    <!-- 已附加项目列表 -->
    <NCard size="small" class="attach-card">
      <template #header>
        <NSpace align="center" :size="8">
          <span class="section-title">已附加到的项目</span>
          <NTag v-if="attachedProjects.length" size="small" type="info">
            {{ attachedProjects.length }}
          </NTag>
        </NSpace>
      </template>
      <NDataTable
        :columns="columns"
        :data="attachedProjects"
        :loading="loading"
        :row-key="row => row.id"
        size="small"
      />
    </NCard>

    <!-- 附加操作 -->
    <NCard v-if="canOperate" size="small" class="attach-card">
      <template #header>
        <span class="section-title">附加到项目</span>
      </template>
      <div class="attach-row">
        <NSelect
          v-model:value="selectedProjectIds"
          :options="availableProjectOptions"
          :loading="projectsLoading"
          multiple
          clearable
          filterable
          placeholder="选择要附加到的项目（已附加当前版本的项目不显示）"
          class="project-select"
        />
        <NButton
          type="primary"
          :loading="attaching"
          :disabled="!latestVersionId || selectedProjectIds.length === 0"
          @click="handleAttach"
        >
          <template #icon>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
              <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
            </svg>
          </template>
          附加
        </NButton>
      </div>
      <div v-if="!latestVersionId" class="attach-tip">
        当前题目尚未发版入库，发版后才能附加到项目
      </div>
    </NCard>
  </div>
</template>

<style scoped lang="scss">
.attach-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.attach-card {
  border-radius: 4px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.attach-row {
  display: flex;
  align-items: center;
  gap: 12px;

  .project-select {
    flex: 1;
    min-width: 0;
  }
}

.attach-tip {
  margin-top: 12px;
  padding: 10px 12px;
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.3);
  border-radius: 4px;
  color: #d97706;
  font-size: 13px;
}
</style>
