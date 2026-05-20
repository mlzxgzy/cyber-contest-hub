<script lang="ts" setup>
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { NButton, NCard, NDataTable, NSpace, useDialog } from 'naive-ui';
import { fetchGetChallengeVersionList } from '@/service/api/cch/challenge-version';
import { fetchImportProjectChallenges, fetchGetProjectChallenges } from '@/service/api/cch/project';
import { useAppStore } from '@/store/modules/app';
import { defaultTransform, useNaivePaginatedTable } from '@/hooks/common/table';
import ChallengeVersionSearch from '@/views/cch/challenge-version/modules/challenge-version-search.vue';

defineOptions({
  name: 'ProjectChallengeImport'
});

const route = useRoute();
const router = useRouter();
const appStore = useAppStore();
const dialog = useDialog();

const projectId = computed(() => route.params.id as string);

const searchParams = ref<Api.Cch.ChallengeVersionSearchParams>({
  pageNum: 1,
  pageSize: 10,
  challengeId: null,
  challengeName: null,
  draftId: null,
  versionTag: null,
  versionDescription: null,
  params: {}
});

const { columns, columnChecks, data, getData, getDataByPage, loading, mobilePagination, scrollX } =
  useNaivePaginatedTable({
    api: () => fetchGetChallengeVersionList(searchParams.value),
    transform: response => defaultTransform(response),
    onPaginationParamsChange: params => {
      searchParams.value.pageNum = params.page;
      searchParams.value.pageSize = params.pageSize;
    },
    columns: () => [
      {
        type: 'selection',
        align: 'center',
        width: 48
      },
      {
        key: 'index',
        title: '序号',
        align: 'center',
        width: 64,
        render: (_, index) => index + 1
      },
      {
        key: 'challengeName',
        title: '题目名称',
        align: 'center',
        minWidth: 150
      },
      {
        key: 'versionTag',
        title: '版本号',
        align: 'center',
        minWidth: 120
      },
      {
        key: 'versionDescription',
        title: '版本描述',
        align: 'center',
        minWidth: 200
      },
      {
        key: 'createTime',
        title: '创建时间',
        align: 'center',
        minWidth: 160
      }
    ]
  });

const checkedRowKeys = ref<CommonType.IdType[]>([]);
const importing = ref(false);
const existingChallengeVersionIds = ref<Set<CommonType.IdType>>(new Set());

// 加载已导入的题目版本ID
async function loadExistingChallenges() {
  if (!projectId.value) return;

  const { data: challenges, error } = await fetchGetProjectChallenges(projectId.value);
  if (!error && challenges) {
    existingChallengeVersionIds.value = new Set(challenges.map(c => c.versionId));
  }
}

// 检查选中的版本是否已经导入过
const hasExistingSelection = computed(() => {
  return checkedRowKeys.value.some(id => existingChallengeVersionIds.value.has(id));
});

// 处理批量导入
async function handleBatchImport() {
  if (checkedRowKeys.value.length === 0) {
    window.$message?.warning('请至少选择一个题目版本');
    return;
  }

  // 检查是否有已导入的版本
  if (hasExistingSelection.value) {
    dialog.warning({
      title: '确认导入',
      content: '选中的版本中包含已导入的题目，是否继续导入？',
      positiveText: '继续导入',
      negativeText: '取消',
      onPositiveClick: async () => {
        await doImport();
      }
    });
  } else {
    await doImport();
  }
}

async function doImport() {
  importing.value = true;

  try {
    const challengesToImport = checkedRowKeys.value.map(versionId => ({ versionId }));
    const { error } = await fetchImportProjectChallenges(projectId.value, challengesToImport);

    if (error) {
      window.$message?.error('导入失败');
      return;
    }

    window.$message?.success(`成功导入 ${checkedRowKeys.value.length} 个题目版本`);

    // 返回项目详情页
    router.push({
      name: 'cch-project-detail',
      params: { id: projectId.value }
    });
  } catch (e) {
    window.$message?.error('导入过程中发生错误');
  } finally {
    importing.value = false;
  }
}

// 返回项目详情页
function goBack() {
  router.push({
    name: 'cch-project-detail',
    params: { id: projectId.value }
  });
}

// 初始化
loadExistingChallenges();
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard :bordered="false" class="card-wrapper sm:flex-1-hidden" size="small">
      <template #header>
        <div class="flex items-center gap-8px">
          <NButton text @click="goBack">
            <template #icon>
              <icon-ic-round-arrow-back class="text-icon" />
            </template>
          </NButton>
          <span class="text-16px font-bold">导入题目到项目</span>
        </div>
      </template>

      <div class="flex flex-col gap-16px h-full">
        <!-- 搜索区域 -->
        <ChallengeVersionSearch v-model:model="searchParams" @search="getDataByPage" />

        <!-- 表格区域 -->
        <NCard :bordered="false" class="card-wrapper sm:flex-1-hidden" size="small" title=" ">
          <template #header-extra>
            <NSpace>
              <NButton
                type="primary"
                :loading="importing"
                :disabled="checkedRowKeys.length === 0"
                @click="handleBatchImport"
              >
                导入选中版本 ({{ checkedRowKeys.length }})
              </NButton>
              <NButton @click="getData">
                <template #icon>
                  <icon-ic-round-refresh class="text-icon" />
                </template>
                刷新
              </NButton>
            </NSpace>
          </template>

          <NDataTable
            v-model:checked-row-keys="checkedRowKeys"
            :columns="columns"
            :data="data"
            :flex-height="!appStore.isMobile"
            :loading="loading"
            :pagination="mobilePagination"
            :row-key="row => row.id"
            :scroll-x="scrollX"
            class="sm:h-full"
            remote
            size="small"
          />
        </NCard>
      </div>
    </NCard>
  </div>
</template>

<style scoped lang="scss">
:deep(.n-card-header) {
  padding: 12px 16px;
}
</style>
