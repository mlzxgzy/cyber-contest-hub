import {request} from '@/service/request';

/** 获取挑战容器镜像列表 */
export function fetchGetChallengeContainerImageList(params?: Api.Cch.ChallengeContainerImageSearchParams) {
  return request<Api.Cch.ChallengeContainerImageList>({
    url: '/cch/challengeContainerImage/list',
    method: 'get',
    params
  });
}

/** 新增挑战容器镜像 */
export function fetchCreateChallengeContainerImage(data: Api.Cch.ChallengeContainerImageOperateParams) {
  return request<boolean>({
    url: '/cch/challengeContainerImage',
    method: 'post',
    data
  });
}

/** 修改挑战容器镜像 */
export function fetchUpdateChallengeContainerImage(data: Api.Cch.ChallengeContainerImageOperateParams) {
  return request<boolean>({
    url: '/cch/challengeContainerImage',
    method: 'put',
    data
  });
}

/** 批量删除挑战容器镜像 */
export function fetchBatchDeleteChallengeContainerImage(ids: CommonType.IdType[]) {
  return request<boolean>({
    url: `/cch/challengeContainerImage/${ids.join(',')}`,
    method: 'delete'
  });
}

/** 根据ID获取挑战容器镜像 */
export function fetchGetChallengeContainerImageById(id: CommonType.IdType) {
  return request<Api.Cch.ChallengeContainerImage>({
    url: `/cch/challengeContainerImage/${id}`,
    method: 'get'
  });
}

/** 根据题目ID获取镜像列表 */
export function fetchGetChallengeContainerImageByChallengeId(challengeId: CommonType.IdType) {
  return request<Api.Cch.ChallengeContainerImage[]>({
    url: `/cch/challengeContainerImage/byChallenge/${challengeId}`,
    method: 'get'
  });
}

/** 上传容器镜像 */
export function fetchUploadChallengeContainerImage(formData: FormData) {
  return request<Api.Cch.ChallengeContainerImage>({
    url: '/cch/challengeContainerImage/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/** 下载容器镜像文件 */
export function fetchDownloadChallengeContainerImage(id: CommonType.IdType) {
  return `/cch/challengeContainerImage/download/${id}`;
}

/** 获取上传进度 */
export function fetchGetUploadProgress(id: CommonType.IdType) {
  return request<{ id: CommonType.IdType; status: string; progress: number; errorMessage?: string }>({
    url: `/cch/challengeContainerImage/progress/${id}`,
    method: 'get'
  });
}

/** 手动Load镜像到Docker */
export function fetchManualLoadImage(id: CommonType.IdType) {
  return request<boolean>({
    url: `/cch/challengeContainerImage/load/${id}`,
    method: 'post'
  });
}
