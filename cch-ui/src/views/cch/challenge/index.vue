<script lang="tsx" setup>
import {ref} from 'vue';
import {useRouter} from 'vue-router';
import {NDivider, NTag} from 'naive-ui';
import {fetchBatchDeleteChallenge, fetchGetChallengeList} from '@/service/api/cch/challenge';
import {useAppStore} from '@/store/modules/app';
import {useAuth} from '@/hooks/business/auth';
import {useDownload} from '@/hooks/business/download';
import {defaultTransform, useNaivePaginatedTable, useTableOperate} from '@/hooks/common/table';
import {$t} from '@/locales';
import ButtonIcon from '@/components/custom/button-icon.vue';
import ChallengeOperateDrawer from './modules/challenge-operate-drawer.vue';
import ChallengeSearch from './modules/challenge-search.vue';

defineOptions({
  name: 'ChallengeList'
});

const router = useRouter();
const appStore = useAppStore();
const {download} = useDownload();
const {hasAuth} = useAuth();

const searchParams = ref<Api.Cch.ChallengeSearchParams>({
  pageNum: 1,
  pageSize: 10,
  category: null,
  name: null,
  remark: null,
  published: null,
  params: {}
});

const {columns, columnChecks, data, getData, getDataByPage, loading, mobilePagination, scrollX} =
  useNaivePaginatedTable({
    api: () => fetchGetChallengeList(searchParams.value),
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
        minWidth: 120,
      },
      {
        key: 'category',
        title: '题目类型',
        align: 'center',
        minWidth: 120
      },
      {
        key: 'name',
        title: '题目名称',
        align: 'center',
        minWidth: 120
      },
      {
        key: 'remark',
        title: '题目备注',
        align: 'center',
        minWidth: 120
      },
      {
        key: 'latestVersionId',
        title: '题目最新版ID',
        align: 'center',
        minWidth: 120
      },
      {
        key: 'status',
        title: '入库状态',
        align: 'center',
        minWidth: 130,
        render: row => {
          if (!row.latestVersionId) {
            return <NTag type="warning" size="small" bordered={false}>草稿</NTag>;
          }
          return (
            <div class="flex-center gap-8px">
              <NTag type="success" size="small" bordered={false}>已入库</NTag>
              {row.latestVersionTag ? (
                <span class="text-12px text-gray-500">{row.latestVersionTag}</span>
              ) : null}
            </div>
          );
        }
      },
      {
        key: 'updateTime',
        title: '更新时间',
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
            if (!hasAuth('cch:challenge:edit') || !hasAuth('cch:challenge:remove')) {
              return null;
            }
            return <NDivider vertical/>;
          };

          const editBtn = () => {
            if (!hasAuth('cch:challenge:edit')) {
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

          const editContentBtn = () => {
            if (!hasAuth('cch:challenge:edit')) {
              return null;
            }
            return (
                <ButtonIcon
                    text
                    type="primary"
                    icon="material-symbols:edit-note-outline"
                    tooltipContent="编辑题目内容"
                    onClick={() => handleEditContent(row.id)}
                />
            );
          };

          const deleteBtn = () => {
            if (!hasAuth('cch:challenge:remove')) {
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
                {editContentBtn()}
                {divider()}
                {deleteBtn()}
              </div>
          );
        }
      }
    ]
  });

// 设置 index id 列默认隐藏
columnChecks.value.forEach(check => {
  if (check.key === 'index') {
    check.checked = false;
  }
  if (check.key === 'id') {
    check.checked = false;
  }
  if (check.key === 'latestVersionId') {
    check.checked = false;
  }
});

const {drawerVisible, operateType, editingData, handleEdit, checkedRowKeys, onBatchDeleted, onDeleted} =
    useTableOperate(data, 'id', getData);

// 新增题目：直接进入"新增题目入库"页面（草稿编辑页 create 模式），不再走三步抽屉
function handleAddChallenge() {
  router.push({
    path: '/cch/challenge/edit',
    query: {
      mode: 'create'
    }
  });
}

async function handleBatchDelete() {
  // request
  const {error} = await fetchBatchDeleteChallenge(checkedRowKeys.value);
  if (error) return;
  onBatchDeleted();
}

async function handleDelete(id: CommonType.IdType) {
  // request
  const {error} = await fetchBatchDeleteChallenge([id]);
  if (error) return;
  onDeleted();
}

function edit(id: CommonType.IdType) {
  handleEdit(id);
}

async function handleEditContent(id: CommonType.IdType) {
  // 跳转到草稿编辑页面，添加refresh参数强制刷新
  await router.push({
    path: '/cch/challenge/edit',
    query: {
      challengeId: id,
      refresh: 'true'
    }
  });
}

function handleExport() {
  download('/cch/challenge/export', searchParams.value, `题目列表_${new Date().getTime()}.xlsx`);
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ChallengeSearch v-model:model="searchParams" @search="getDataByPage"/>
    <NCard :bordered="false" class="card-wrapper sm:flex-1-hidden" size="small" title="题目列表">
      <template #header-extra>
        <TableHeaderOperation
            v-model:columns="columnChecks"
            :disabled-delete="checkedRowKeys.length === 0"
            :loading="loading"
            :show-add="hasAuth('cch:challenge:add')"
            :show-delete="hasAuth('cch:challenge:remove')"
            :show-export="hasAuth('cch:challenge:export')"
            @add="handleAddChallenge"
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
      <ChallengeOperateDrawer
          v-model:visible="drawerVisible"
          :operate-type="operateType"
          :row-data="editingData"
          @submitted="getDataByPage"
      />
    </NCard>
  </div>
</template>

<style scoped></style>
