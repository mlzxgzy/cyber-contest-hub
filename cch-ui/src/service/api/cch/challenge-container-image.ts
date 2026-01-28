import {request} from '@/service/request';

/** 批量删除挑战容器镜像 */
export function fetchBatchDeleteChallengeContainerImage(ids: CommonType.IdType[]) {
  return request<boolean>({
    url: `/cch/challengeContainerImage/${ids.join(',')}`,
    method: 'delete'
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
    },
    timeout: 300 * 1000
  });
}

/** 手动Load镜像到Docker */
export function fetchManualLoadImage(id: CommonType.IdType) {
  return request<boolean>({
    url: `/cch/challengeContainerImage/load/${id}`,
    method: 'post',
    timeout: 300 * 1000
  });
}
