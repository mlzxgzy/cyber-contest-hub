<script lang="tsx" setup>
import {ref} from 'vue';
import {NDivider, NTag} from 'naive-ui';
import {
  fetchBatchDeleteChallengeContainerImage,
  fetchGetChallengeContainerImageList
} from '@/service/api/cch/challenge-container-image';
import {useAppStore} from '@/store/modules/app';
import {useAuth} from '@/hooks/business/auth';
import {useDownload} from '@/hooks/business/download';
import {defaultTransform, useNaivePaginatedTable, useTableOperate} from '@/hooks/common/table';
import {$t} from '@/locales';
import ButtonIcon from '@/components/custom/button-icon.vue';
import ChallengeContainerImageSearch from './modules/challenge-container-image-search.vue';

defineOptions({
  name: 'ChallengeContainerImageList'
});

const appStore = useAppStore();
const {download} = useDownload();
const {hasAuth} = useAuth();

const searchParams = ref<Api.Cch.ChallengeContainerImageSearchParams>({
  pageNum: 1,
  pageSize: 10,
  challengeId: null,
  imageName: null,
  status: null,
  params: {}
});

const {columns, columnChecks, data, getData, getDataByPage, loading, mobilePagination, scrollX} =
  useNaivePaginatedTable({
    api: () => fetchGetChallengeContainerImageList(searchParams.value),
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
        key: 'imageName',
        title: '镜像名称',
        align: 'center',
        minWidth: 150
      },
      {
        key: 'imageTag',
        title: '镜像标签',
        align: 'center',
        minWidth: 100
      },
      {
        key: 'imageSize',
        title: '镜像大小',
        align: 'center',
        minWidth: 120,
        render: row => {
          const size = row.imageSize;
          if (!size) return '-';
          // 转换为人类可读格式
          const units = ['B', 'KB', 'MB', 'GB', 'TB'];
          let unitIndex = 0;
          let sizeNum = size;
          while (sizeNum >= 1024 && unitIndex < units.length - 1) {
            sizeNum /= 1024;
            unitIndex++;
          }
          return `${sizeNum.toFixed(2)} ${units[unitIndex]}`;
        }
      },
      {
        key: 'status',
        title: '状态',
        align: 'center',
        minWidth: 100,
        render: row => {
          const statusMap: Record<string, {
            type: 'default' | 'success' | 'warning' | 'error' | 'info';
            label: string
          }> = {
            uploading: {type: 'warning', label: '上传中'},
            uploaded: {type: 'info', label: '已上传'},
            validating: {type: 'info', label: '验证中'},
            available: {type: 'success', label: '可用'},
            error: {type: 'error', label: '错误'}
          };
          const status = statusMap[row.status] || {type: 'default', label: row.status};
          return <NTag type={status.type} size="small">{status.label}</NTag>;
        }
      },
      {
        key: 'progress',
        title: '上传进度',
        align: 'center',
        minWidth: 120,
        render: row => {
          if (row.progress === null || row.progress === undefined) return '-';
          return `${row.progress}%`;
        }
      },
      {
        key: 'createTime',
        title: '创建时间',
        align: 'center',
        minWidth: 180
      },
      {
        key: 'operate',
        title: $t('common.operate'),
        align: 'center',
        width: 130,
        render: row => {
          const divider = () => {
            if (!hasAuth('cch:challengeContainerImage:edit') && !hasAuth('cch:challengeContainerImage:remove')) {
              return null;
            }
            return <NDivider vertical/>;
          };

          const deleteBtn = () => {
            if (!hasAuth('cch:challengeContainerImage:remove')) {
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
});

const {checkedRowKeys, onBatchDeleted, onDeleted} = useTableOperate(data, 'id', getData);

async function handleBatchDelete() {
  const {error} = await fetchBatchDeleteChallengeContainerImage(checkedRowKeys.value);
  if (error) return;
  onBatchDeleted();
}

async function handleDelete(id: CommonType.IdType) {
  const {error} = await fetchBatchDeleteChallengeContainerImage([id]);
  if (error) return;
  onDeleted();
}

function handleExport() {
  download('/cch/challengeContainerImage/export', searchParams, `容器镜像列表_${new Date().getTime()}.xlsx`);
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ChallengeContainerImageSearch v-model:model="searchParams" @search="getDataByPage"/>
    <NCard :bordered="false" class="card-wrapper sm:flex-1-hidden" size="small" title="容器镜像列表">
      <template #header-extra>
        <TableHeaderOperation
          v-model:columns="columnChecks"
          :disabled-delete="checkedRowKeys.length === 0"
          :loading="loading"
          :show-delete="hasAuth('cch:challengeContainerImage:remove')"
          :show-export="hasAuth('cch:challengeContainerImage:export')"
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
    </NCard>
  </div>
</template>

<style scoped></style>
