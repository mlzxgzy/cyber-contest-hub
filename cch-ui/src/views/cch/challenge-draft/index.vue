<script lang="tsx" setup>
import {ref} from 'vue';
import {NDivider} from 'naive-ui';
import {fetchBatchDeleteChallengeDraft, fetchGetChallengeDraftList} from '@/service/api/cch/challenge-draft';
import {useAppStore} from '@/store/modules/app';
import {useAuth} from '@/hooks/business/auth';
import {useDownload} from '@/hooks/business/download';
import {defaultTransform, useNaivePaginatedTable, useTableOperate} from '@/hooks/common/table';
import {$t} from '@/locales';
import ButtonIcon from '@/components/custom/button-icon.vue';
import ChallengeDraftOperateDrawer from './modules/challenge-draft-operate-drawer.vue';
import ChallengeDraftSearch from './modules/challenge-draft-search.vue';

defineOptions({
  name: 'ChallengeDraftList'
});

const appStore = useAppStore();
const {download} = useDownload();
const {hasAuth} = useAuth();

const searchParams = ref<Api.Cch.ChallengeDraftSearchParams>({
  pageNum: 1,
  pageSize: 10,
  challengeId: null,
  challengeName: null,
  challengeDescription: null,
  params: {}
});

const {columns, columnChecks, data, getData, getDataByPage, loading, mobilePagination, scrollX} =
  useNaivePaginatedTable({
    api: () => fetchGetChallengeDraftList(searchParams.value),
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
        key: 'challengeDescription',
        title: '草稿描述',
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
            if (!hasAuth('cch:challengeDraft:edit') || !hasAuth('cch:challengeDraft:remove')) {
              return null;
            }
            return <NDivider vertical/>;
          };

          const editBtn = () => {
            if (!hasAuth('cch:challengeDraft:edit')) {
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
            if (!hasAuth('cch:challengeDraft:remove')) {
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
  const {error} = await fetchBatchDeleteChallengeDraft(checkedRowKeys.value);
  if (error) return;
  onBatchDeleted();
}

async function handleDelete(id: CommonType.IdType) {
  // request
  const {error} = await fetchBatchDeleteChallengeDraft([id]);
  if (error) return;
  onDeleted();
}

function edit(id: CommonType.IdType) {
  handleEdit(id);
}

function handleExport() {
  download('/cch/challengeDraft/export', searchParams.value, `题目草稿_${new Date().getTime()}.xlsx`);
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ChallengeDraftSearch v-model:model="searchParams" @search="getDataByPage"/>
    <NCard :bordered="false" class="card-wrapper sm:flex-1-hidden" size="small" title="题目草稿列表">
      <template #header-extra>
        <TableHeaderOperation
          v-model:columns="columnChecks"
          :disabled-delete="checkedRowKeys.length === 0"
          :loading="loading"
          :show-add="hasAuth('cch:challengeDraft:add')"
          :show-delete="hasAuth('cch:challengeDraft:remove')"
          :show-export="hasAuth('cch:challengeDraft:export')"
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
      <ChallengeDraftOperateDrawer
        v-model:visible="drawerVisible"
        :operate-type="operateType"
        :row-data="editingData"
        @submitted="getDataByPage"
      />
    </NCard>
  </div>
</template>

<style scoped></style>
