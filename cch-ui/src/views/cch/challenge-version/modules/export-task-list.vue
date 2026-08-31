<script lang="tsx" setup>
import {ref, onMounted, onUnmounted} from 'vue';
import {NTag, NTooltip} from 'naive-ui';
import {
  fetchGetExportTaskList,
  fetchGetExportTaskDownloadUrl,
  fetchBatchDeleteExportTask,
  fetchRetryExportTask
} from '@/service/api/cch/challenge-version-export';
import {useAuth} from '@/hooks/business/auth';
import {useDownload} from '@/hooks/business/download';
import {defaultTransform, useNaivePaginatedTable, useTableOperate} from '@/hooks/common/table';
import {$t} from '@/locales';
import ButtonIcon from '@/components/custom/button-icon.vue';

defineOptions({
  name: 'ExportTaskList'
});

const {hasAuth} = useAuth();
const {zip} = useDownload();

const searchParams = ref<Api.Cch.ExportTaskSearchParams>({
  pageNum: 1,
  pageSize: 10,
  versionId: null,
  versionTag: null,
  taskStatus: null,
  params: {}
});

const {columns, columnChecks, data, getData, getDataByPage, loading, mobilePagination, scrollX} =
  useNaivePaginatedTable({
    api: () => fetchGetExportTaskList(searchParams.value),
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
        key: 'challengeName',
        title: '题目名称',
        align: 'center',
        minWidth: 200,
        render: row => {
          const challengeName = row.challengeName || '';
          const versionTag = row.versionTag || '';
          if (challengeName && versionTag) {
            return `${challengeName}:${versionTag}`;
          } else if (challengeName) {
            return challengeName;
          } else if (versionTag) {
            return versionTag;
          }
          return '-';
        }
      },
      {
        key: 'taskStatusText',
        title: '任务状态',
        align: 'center',
        width: 100,
        render: row => {
          const status = row.taskStatus;
          const statusText = row.taskStatusText || '未知';
          const errorMessage = row.errorMessage;
          let type: 'default' | 'info' | 'success' | 'warning' | 'error' = 'default';

          if (status === 0) {
            type = 'info';
          } else if (status === 1) {
            type = 'warning';
          } else if (status === 2) {
            type = 'success';
          } else if (status === 3) {
            type = 'error';
          }

          const tag = <NTag type={type}>{statusText}</NTag>;

          // 如果状态为失败且有错误信息，则添加 tooltip（含重试次数提示）
          if (status === 3 && errorMessage) {
            const retryInfo = row.retryCount ? `（已重试 ${row.retryCount} 次）` : '';
            return (
              <NTooltip trigger="hover">
                {{
                  default: () => `${errorMessage}${retryInfo}`,
                  trigger: () => tag
                }}
              </NTooltip>
            );
          }

          return tag;
        }
      },
      {
        key: 'fileSizeText',
        title: '文件大小',
        align: 'center',
        width: 100,
        render: row => row.fileSizeText || '-'
      },
      {
        key: 'createTime',
        title: '创建时间',
        align: 'center',
        width: 180
      },
      {
        key: 'expireTime',
        title: '过期时间',
        align: 'center',
        width: 180,
        render: row => row.expireTime || '-'
      },
      {
        key: 'operate',
        title: $t('common.operate'),
        align: 'center',
        width: 160,
        render: row => {
          const downloadBtn = () => {
            if (row.taskStatus !== 2) {
              return null; // 只有已完成的任务才能下载
            }
            if (!hasAuth('cch:challengeVersion:export')) {
              return null;
            }
            return (
              <ButtonIcon
                text
                type="primary"
                icon="material-symbols:download"
                tooltipContent="下载"
                onClick={() => handleDownload(row.id)}
              />
            );
          };

          const retryBtn = () => {
            if (row.taskStatus !== 3) {
              return null; // 只有失败的任务才能重试
            }
            if (!hasAuth('cch:challengeVersion:export')) {
              return null;
            }
            return (
              <ButtonIcon
                text
                type="warning"
                icon="material-symbols:refresh"
                tooltipContent="重试"
                popconfirmContent={`确定重试该导出任务吗？`}
                onPositiveClick={() => handleRetry(row.id)}
              />
            );
          };

          const deleteBtn = () => {
            if (!hasAuth('cch:challengeVersion:export')) {
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
              {downloadBtn()}
              {retryBtn()}
              {deleteBtn()}
            </div>
          );
        }
      }
    ]
  });

const {checkedRowKeys, onBatchDeleted, onDeleted} = useTableOperate(data, 'id', getData);

let refreshTimer: number | null = null;

onMounted(() => {
  getData();
  // 每5秒自动刷新一次，以便实时查看任务状态
  refreshTimer = window.setInterval(() => {
    if (data.value.some(item => item.taskStatus === 0 || item.taskStatus === 1)) {
      // 如果有待处理或处理中的任务，自动刷新
      getData();
    }
  }, 5000);
});

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
  }
});

async function handleDownload(taskId: CommonType.IdType) {
  try {
    const {response: {data: data}} = await fetchGetExportTaskDownloadUrl(taskId);
    var downloadUrl = data.msg;
    if (downloadUrl) {
      // 从URL中提取文件名
      const urlParts = downloadUrl.split('/');
      const fileName = urlParts[urlParts.length - 1] || `export_${taskId}.zip`;
      // 如果后端返回的是完整URL（例如OSS临时链接），直接使用浏览器下载
      const isAbsoluteUrl = /^https?:\/\//i.test(downloadUrl);
      if (isAbsoluteUrl) {
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.style.display = 'none';
        // 对于同源链接可以设置 download 文件名，跨域时即使不生效也能触发下载/打开
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      } else {
        // 相对路径仍然走统一的下载封装，带上鉴权等头信息
        zip(downloadUrl, fileName);
      }
    }
  } catch (error) {
    window.$message?.error('获取下载链接失败');
  }
}

async function handleDelete(id: CommonType.IdType) {
  const {error} = await fetchBatchDeleteExportTask([id]);
  if (error) return;
  onDeleted();
}

async function handleRetry(taskId: CommonType.IdType) {
  const {error} = await fetchRetryExportTask(taskId);
  if (error) return;
  window.$message?.success('已重新加入导出队列');
  getData();
}

async function handleBatchDelete() {
  const {error} = await fetchBatchDeleteExportTask(checkedRowKeys.value);
  if (error) return;
  onBatchDeleted();
}

function handleSearch() {
  getDataByPage();
}

function handleReset() {
  searchParams.value = {
    pageNum: 1,
    pageSize: 10,
    versionId: null,
    versionTag: null,
    taskStatus: null,
    params: {}
  };
  getDataByPage();
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard :bordered="false" class="card-wrapper" size="small">
      <NCollapse>
        <NCollapseItem title="搜索" name="export-task-search">
          <NForm :label-width="80" :model="searchParams" label-placement="left">
            <NGrid item-responsive responsive="screen">
              <NFormItemGi class="pr-24px" label="版本ID" label-width="auto" path="versionId" span="24 s:12 m:6">
                <NInput v-model:value="searchParams.versionId" placeholder="请输入版本ID"/>
              </NFormItemGi>
              <NFormItemGi class="pr-24px" label="版本号" label-width="auto" path="versionTag" span="24 s:12 m:6">
                <NInput v-model:value="searchParams.versionTag" placeholder="请输入版本号"/>
              </NFormItemGi>
              <NFormItemGi class="pr-24px" label="任务状态" label-width="auto" path="taskStatus" span="24 s:12 m:6">
                <NSelect
                  v-model:value="searchParams.taskStatus"
                  placeholder="请选择任务状态"
                  clearable
                  :options="[
                    {label: '待处理', value: 0},
                    {label: '处理中', value: 1},
                    {label: '已完成', value: 2},
                    {label: '失败', value: 3}
                  ]"
                />
              </NFormItemGi>
              <NFormItemGi :show-feedback="false" class="pr-24px" span="24">
                <NSpace class="w-full" justify="end">
                  <NButton @click="handleReset">
                    <template #icon>
                      <icon-ic-round-refresh class="text-icon"/>
                    </template>
                    {{ $t('common.reset') }}
                  </NButton>
                  <NButton ghost type="primary" @click="handleSearch">
                    <template #icon>
                      <icon-ic-round-search class="text-icon"/>
                    </template>
                    {{ $t('common.search') }}
                  </NButton>
                </NSpace>
              </NFormItemGi>
            </NGrid>
          </NForm>
        </NCollapseItem>
      </NCollapse>
    </NCard>
    <NCard :bordered="false" class="card-wrapper sm:flex-1-hidden" size="small" title="导出任务列表">
      <template #header-extra>
        <TableHeaderOperation
          v-model:columns="columnChecks"
          :disabled-delete="checkedRowKeys.length === 0"
          :loading="loading"
          :show-add="false"
          :show-delete="hasAuth('cch:challengeVersion:export')"
          :show-export="false"
          @delete="handleBatchDelete"
          @refresh="getData"
        />
      </template>
      <NDataTable
        v-model:checked-row-keys="checkedRowKeys"
        :columns="columns"
        :data="data"
        :flex-height="false"
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
