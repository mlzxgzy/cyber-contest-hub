<script lang="tsx" setup>
import {ref} from 'vue';
import {NDivider, NTabs, NTabPane} from 'naive-ui';
import {useDialog} from 'naive-ui';
import {fetchBatchDeleteChallengeVersion, fetchGetChallengeVersionList} from '@/service/api/cch/challenge-version';
import {fetchCreateExportTasks} from '@/service/api/cch/challenge-version-export';
import {useAppStore} from '@/store/modules/app';
import {useAuth} from '@/hooks/business/auth';
import {useDownload} from '@/hooks/business/download';
import {defaultTransform, useNaivePaginatedTable, useTableOperate} from '@/hooks/common/table';
import {$t} from '@/locales';
import ButtonIcon from '@/components/custom/button-icon.vue';
import ChallengeVersionOperateDrawer from './modules/challenge-version-operate-drawer.vue';
import ChallengeVersionSearch from './modules/challenge-version-search.vue';
import ExportTaskList from './modules/export-task-list.vue';

defineOptions({
  name: 'ChallengeVersionList'
});

const appStore = useAppStore();
const {download} = useDownload();
const {hasAuth} = useAuth();
const dialog = useDialog();

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

const {columns, columnChecks, data, getData, getDataByPage, loading, mobilePagination, scrollX} =
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
        title: $t('common.index'),
        align: 'center',
        width: 64,
        render: (_, index) => index + 1
      },
      {
        key: 'id',
        title: '主键',
        align: 'center',
        minWidth: 120
      },
      {
        key: 'challengeId',
        title: '题目ID',
        align: 'center',
        minWidth: 120
      },
      {
        key: 'challengeName',
        title: '题目名称',
        align: 'center',
        minWidth: 120
      },
      {
        key: 'draftId',
        title: '草稿ID',
        align: 'center',
        minWidth: 120
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
        minWidth: 120
      },
      {
        key: 'operate',
        title: $t('common.operate'),
        align: 'center',
        width: 130,
        render: row => {
          const divider = () => {
            if (!hasAuth('cch:challengeVersion:edit') || !hasAuth('cch:challengeVersion:remove')) {
              return null;
            }
            return <NDivider vertical/>;
          };

          const editBtn = () => {
            if (!hasAuth('cch:challengeVersion:edit')) {
              return null;
            }
            return (
              <ButtonIcon
                text
                type="primary"
                icon="material-symbols:drive-file-rename-outline-outline"
                tooltipContent={$t('common.edit')}
                onClick={() => edit(row.id)}
              />
            );
          };

          const deleteBtn = () => {
            if (!hasAuth('cch:challengeVersion:remove')) {
              return null;
            }
            return (
              <ButtonIcon
                text
                type="error"
                icon="material-symbols:delete-outline"
                tooltipContent={$t('common.delete')}
                popconfirmContent={$t('common.confirmDelete')}
                onPositiveClick={() => handleDelete(row.id)}
              />
            );
          };

          return (
            <div class="flex-center gap-8px">
              {editBtn()}
              {divider()}
              {deleteBtn()}
            </div>
          );
        }
      }
    ]
  });

const {drawerVisible, operateType, editingData, handleAdd, handleEdit, checkedRowKeys, onBatchDeleted, onDeleted} =
  useTableOperate(data, 'id', getData);

async function handleBatchDelete() {
  // request
  const {error} = await fetchBatchDeleteChallengeVersion(checkedRowKeys.value);
  if (error) return;
  onBatchDeleted();
}

async function handleDelete(id: CommonType.IdType) {
  // request
  const {error} = await fetchBatchDeleteChallengeVersion([id]);
  if (error) return;
  onDeleted();
}

function edit(id: CommonType.IdType) {
  handleEdit(id);
}

const activeTab = ref<string>('version-list');

function handleExport() {
  // 如果没有选中，使用原有的同步导出方式（向后兼容）
  if (checkedRowKeys.value.length === 0) {
    download('/cch/challengeVersion/export', searchParams.value, `题目版本_${new Date().getTime()}.xlsx`);
    return;
  }

  // 弹出对话框，询问是否导出容器镜像文件
  dialog.info({
    title: '导出选项',
    content: () => (
      <div class="flex flex-col gap-12px py-4px">
        <p class="text-14px">
          是否将容器镜像文件（.tar.gz）一并打包到导出压缩包中？
        </p>
        <ul class="text-13px text-gray-500 list-disc pl-16px leading-relaxed">
          <li>
            <span class="font-medium text-gray-700">导出镜像文件</span>
            ：压缩包较大，但可在无网络环境下直接加载镜像
          </li>
          <li>
            <span class="font-medium text-gray-700">仅保留镜像地址</span>
            ：压缩包更小，镜像地址记录在 challenge_info.json 中
          </li>
        </ul>
      </div>
    ),
    positiveText: '导出镜像文件',
    negativeText: '仅保留镜像地址',
    onPositiveClick: () => doCreateExportTasks(true),
    onNegativeClick: () => doCreateExportTasks(false)
  });
}

async function doCreateExportTasks(includeImages: boolean) {
  try {
    const {data: taskIds} = await fetchCreateExportTasks(checkedRowKeys.value, includeImages);
    if (taskIds && taskIds.length > 0) {
      window.$message?.success(`已创建 ${taskIds.length} 个导出任务，请切换到"导出任务"标签页查看进度`);
      activeTab.value = 'export-task';
    }
  } catch (error) {
    window.$message?.error('创建导出任务失败');
  }
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard :bordered="false" class="card-wrapper sm:flex-1-hidden" size="small">
      <NTabs v-model:value="activeTab" type="line" animated>
        <NTabPane name="version-list" tab="题目版本列表">
          <div class="flex flex-col gap-16px h-full">
            <ChallengeVersionSearch v-model:model="searchParams" @search="getDataByPage"/>
            <NCard :bordered="false" class="card-wrapper sm:flex-1-hidden" size="small" title=" ">
              <template #header-extra>
                <TableHeaderOperation
                  v-model:columns="columnChecks"
                  :disabled-delete="checkedRowKeys.length === 0"
                  :loading="loading"
                  :show-add="hasAuth('cch:challengeVersion:add')"
                  :show-delete="hasAuth('cch:challengeVersion:remove')"
                  :show-export="hasAuth('cch:challengeVersion:export')"
                  @add="handleAdd"
                  @delete="handleBatchDelete"
                  @export="handleExport"
                  @refresh="getData"
                />
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
              <ChallengeVersionOperateDrawer
                v-model:visible="drawerVisible"
                :operate-type="operateType"
                :row-data="editingData"
                @submitted="getDataByPage"
              />
            </NCard>
          </div>
        </NTabPane>
        <NTabPane name="export-task" tab="导出任务">
          <ExportTaskList/>
        </NTabPane>
      </NTabs>
    </NCard>
  </div>
</template>

<style scoped lang="scss">
// 确保 NTabs 内容区域能正确占据高度
:deep(.n-tabs) {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

:deep(.n-tabs-content) {
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

:deep(.n-tabs-content--animated) {
  height: 100%;
}

:deep(.n-tabs-pane-wrapper) {
  flex: 1;
  height: 100%;
  overflow: hidden;
  min-height: 0;
}

:deep(.n-tab-pane) {
  height: 100%;
  overflow: hidden;
}
</style>
