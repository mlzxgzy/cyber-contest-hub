<script lang="tsx" setup>
import {ref} from 'vue';
import {useRouter} from 'vue-router';
import {NDivider} from 'naive-ui';
import {fetchDeleteProject, fetchGetProjectList} from '@/service/api/cch/project';
import {useAppStore} from '@/store/modules/app';
import {useAuth} from '@/hooks/business/auth';
import {useDownload} from '@/hooks/business/download';
import {defaultTransform, useNaivePaginatedTable, useTableOperate} from '@/hooks/common/table';
import {$t} from '@/locales';
import ButtonIcon from '@/components/custom/button-icon.vue';
import ProjectOperateDrawer from './modules/project-operate-drawer.vue';
import ProjectSearch from './modules/project-search.vue';

defineOptions({
  name: 'ProjectList'
});

const router = useRouter();
const appStore = useAppStore();
const {download} = useDownload();
const {hasAuth} = useAuth();

const searchParams = ref<Api.Cch.ProjectSearchParams>({
  pageNum: 1,
  pageSize: 10,
  projectType: null,
  name: null,
  remark: null,
  params: {}
});

const {columns, columnChecks, data, getData, getDataByPage, loading, mobilePagination, scrollX} =
  useNaivePaginatedTable({
    api: () => fetchGetProjectList(searchParams.value),
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
        key: 'projectType',
        title: '项目类型',
        align: 'center',
        minWidth: 100,
        render: row => {
          return row.projectType === 'contest' ? '竞赛项目' : '普通项目';
        }
      },
      {
        key: 'name',
        title: '项目名称',
        align: 'center',
        minWidth: 150
      },
      {
        key: 'remark',
        title: '备注',
        align: 'center',
        minWidth: 150
      },
      {
        key: 'createTime',
        title: '创建时间',
        align: 'center',
        minWidth: 160
      },
      {
        key: 'operate',
        title: $t('common.operate'),
        align: 'center',
        width: 120,
        render: row => {
          const divider = () => {
            if (!hasAuth('cch:project:remove')) {
              return null;
            }
            return <NDivider vertical/>;
          };

          const detailBtn = () => {
            if (!hasAuth('cch:project:query')) {
              return null;
            }
            return (
              <ButtonIcon
                text
                type="primary"
                icon="material-symbols:visibility-outline"
                tooltipContent="查看详情"
                onClick={() => handleViewDetail(row.id)}
              />
            );
          };

          const deleteBtn = () => {
            if (!hasAuth('cch:project:remove')) {
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
              {detailBtn()}
              {divider()}
              {deleteBtn()}
            </div>
          );
        }
      }
    ]
  });

// 设置 index 列默认隐藏
columnChecks.value.forEach(check => {
  if (check.key === 'index') {
    check.checked = false;
  }
});

const {drawerVisible, handleAdd, checkedRowKeys, onBatchDeleted, onDeleted} =
  useTableOperate(data, 'id', getData);

async function handleBatchDelete() {
  const {error} = await fetchDeleteProject(checkedRowKeys.value);
  if (error) return;
  onBatchDeleted();
}

async function handleDelete(id: CommonType.IdType) {
  const {error} = await fetchDeleteProject([id]);
  if (error) return;
  onDeleted();
}

function handleViewDetail(id: CommonType.IdType) {
  // 确保 ID 以字符串形式传递，避免大整数精度丢失
  router.push({
    path: `/cch/project/${String(id)}`
  });
}

/** 新增成功后直接进入项目详情，其余信息在详情页补充 */
function handleCreated(id: CommonType.IdType) {
  router.push({
    path: `/cch/project/${String(id)}`
  });
}

function handleExport() {
  download('/cch/project/export', searchParams.value, `项目列表_${new Date().getTime()}.xlsx`);
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ProjectSearch v-model:model="searchParams" @search="getDataByPage"/>
    <NCard :bordered="false" class="card-wrapper sm:flex-1-hidden" size="small" title="项目列表">
      <template #header-extra>
        <TableHeaderOperation
          v-model:columns="columnChecks"
          :disabled-delete="checkedRowKeys.length === 0"
          :loading="loading"
          :show-add="hasAuth('cch:project:add')"
          :show-delete="hasAuth('cch:project:remove')"
          :show-export="hasAuth('cch:project:export')"
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
        class="sm:h-full project-table"
        remote
        size="small"
      />
      <ProjectOperateDrawer
        v-model:visible="drawerVisible"
        @submitted="handleCreated"
      />
    </NCard>
  </div>
</template>

<style scoped>
/* 柔和化表格行 hover 高亮，避免整行变蓝刺眼 */
.project-table :deep(.n-data-table-td--hover),
.project-table :deep(.n-data-table-tr:hover > .n-data-table-td) {
  background-color: rgba(0, 0, 0, 0.02);
}
</style>
