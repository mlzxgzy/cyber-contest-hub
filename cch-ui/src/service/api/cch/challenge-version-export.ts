import {request} from '@/service/request';

/** 创建导出任务（单个） */
export function fetchCreateExportTask(versionId: CommonType.IdType, includeImages = false) {
  return request<CommonType.IdType>({
    url: '/cch/challengeVersion/export/create',
    method: 'post',
    params: {versionId, includeImages}
  });
}

/** 批量创建导出任务 */
export function fetchCreateExportTasks(versionIds: CommonType.IdType[], includeImages = false) {
  return request<CommonType.IdType[]>({
    url: '/cch/challengeVersion/export/createBatch',
    method: 'post',
    params: {includeImages},
    data: versionIds
  });
}

/** 获取导出任务列表 */
export function fetchGetExportTaskList(params?: Api.Cch.ExportTaskSearchParams) {
  return request<Api.Cch.ExportTaskList>({
    url: '/cch/challengeVersion/export/task/list',
    method: 'get',
    params
  });
}

/** 获取下载链接 */
export function fetchGetExportTaskDownloadUrl(taskId: CommonType.IdType) {
  return request<string>({
    url: `/cch/challengeVersion/export/task/${taskId}/download`,
    method: 'get'
  });
}

/** 重试失败的导出任务 */
export function fetchRetryExportTask(taskId: CommonType.IdType) {
  return request<boolean>({
    url: `/cch/challengeVersion/export/task/${taskId}/retry`,
    method: 'post'
  });
}

/** 批量删除导出任务 */
export function fetchBatchDeleteExportTask(ids: CommonType.IdType[]) {
  return request<boolean>({
    url: `/cch/challengeVersion/export/task/${ids.join(',')}`,
    method: 'delete'
  });
}
