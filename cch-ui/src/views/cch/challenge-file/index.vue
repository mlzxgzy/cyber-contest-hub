<script setup lang="tsx">
import {ref} from 'vue';
import {NButton, NDivider} from 'naive-ui';
import {fetchBatchDeleteChallengeFile, fetchGetChallengeFileList} from '@/service/api/cch/challenge-file';
import {useAppStore} from '@/store/modules/app';
import {useAuth} from '@/hooks/business/auth';
import {useDownload} from '@/hooks/business/download';
import {defaultTransform, useNaivePaginatedTable, useTableOperate} from '@/hooks/common/table';
import {$t} from '@/locales';
import ButtonIcon from '@/components/custom/button-icon.vue';
import ChallengeFileOperateDrawer from './modules/challenge-file-operate-drawer.vue';
import ChallengeFileSearch from './modules/challenge-file-search.vue';
import {useBoolean} from "~/packages/hooks";
import OssUploadModal from "@/views/system/oss/modules/oss-upload-modal.vue";
import ChallengeFileUploadModule from "@/views/cch/challenge-file/modules/challenge-file-upload-module.vue";

defineOptions({
  name: 'ChallengeFileList'
});

const appStore = useAppStore();
const {download} = useDownload();
const {hasAuth} = useAuth();

const searchParams = ref<Api.Cch.ChallengeFileSearchParams>({
  pageNum: 1,
  pageSize: 10,
  challengeId: null,
  fileName: null,
  originalName: null,
  fileSuffix: null,
  url: null,
  service: null,
  params: {}
});

const {columns, columnChecks, data, getData, getDataByPage, loading, mobilePagination, scrollX} =
    useNaivePaginatedTable({
      api: () => fetchGetChallengeFileList(searchParams.value),
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
          title: '题目id',
          align: 'center',
          minWidth: 120
        },
        {
          key: 'fileName',
          title: '文件名',
          align: 'center',
          minWidth: 120
        },
        {
          key: 'originalName',
          title: '原名',
          align: 'center',
          minWidth: 120
        },
        {
          key: 'fileSuffix',
          title: '文件后缀名',
          align: 'center',
          minWidth: 120
        },
        {
          key: 'url',
          title: 'URL地址',
          align: 'center',
          minWidth: 120
        },
        {
          key: 'createTime',
          title: '创建时间',
          align: 'center',
          minWidth: 120
        },
        {
          key: 'service',
          title: '服务商',
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
              if (!hasAuth('cch:challengeFile:edit') || !hasAuth('cch:challengeFile:remove')) {
                return null;
              }
              return <NDivider vertical/>;
            };

            const editBtn = () => {
              if (!hasAuth('cch:challengeFile:edit')) {
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
              if (!hasAuth('cch:challengeFile:remove')) {
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
  const {error} = await fetchBatchDeleteChallengeFile(checkedRowKeys.value);
  if (error) return;
  onBatchDeleted();
}

async function handleDelete(id: CommonType.IdType) {
  // request
  const {error} = await fetchBatchDeleteChallengeFile([id]);
  if (error) return;
  onDeleted();
}

function edit(id: CommonType.IdType) {
  handleEdit(id);
}

function handleExport() {
  download('/cch/challengeFile/export', searchParams, `题目文件_${new Date().getTime()}.xlsx`);
}

const {bool: uploadVisible, setTrue: showFUploadModal} = useBoolean(false);
const fileUploadType = ref<'attachment' | 'writeup'>('attachment');

function handleUpload(type: 'attachment' | 'writeup') {
  fileUploadType.value = type;
  showFUploadModal();
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ChallengeFileSearch v-model:model="searchParams" @search="getDataByPage"/>
    <NCard title="题目文件列表" :bordered="false" size="small" class="card-wrapper sm:flex-1-hidden">
      <template #header-extra>
        <TableHeaderOperation
            v-model:columns="columnChecks"
            :disabled-delete="checkedRowKeys.length === 0"
            :loading="loading"
            :show-add="hasAuth('cch:challengeFile:add')"
            :show-delete="hasAuth('cch:challengeFile:remove')"
            :show-export="hasAuth('cch:challengeFile:export')"
            @add="handleAdd"
            @delete="handleBatchDelete"
            @export="handleExport"
            @refresh="getData"
        >
          <template #prefix>
            <NButton size="small" ghost @click="handleUpload('attachment')">
              <template #icon>
                <icon-material-symbols-upload-rounded/>
              </template>
              上传附件
            </NButton>
            <NButton size="small" ghost @click="handleUpload('writeup')">
              <template #icon>
                <icon-material-symbols-image-outline/>
              </template>
              上传Writeup
            </NButton>
          </template>
        </TableHeaderOperation>
      </template>
      <NDataTable
          v-model:checked-row-keys="checkedRowKeys"
          :columns="columns"
          :data="data"
          size="small"
          :flex-height="!appStore.isMobile"
          :scroll-x="scrollX"
          :loading="loading"
          remote
          :row-key="row => row.id"
          :pagination="mobilePagination"
          class="sm:h-full"
      />
      <ChallengeFileOperateDrawer
          v-model:visible="drawerVisible"
          :operate-type="operateType"
          :row-data="editingData"
          @submitted="getDataByPage"
      />
      <challenge-file-upload-module v-model:visible="uploadVisible" :upload-type="fileUploadType"
                                    :show-challenge-id="true" @close="getDataByPage"/>
    </NCard>
  </div>
</template>

<style scoped></style>
