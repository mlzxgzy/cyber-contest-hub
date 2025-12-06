import {request} from '@/service/request';

/** 获取题目版本列表 */
export function fetchGetChallengeVersionList(params?: Api.Cch.ChallengeVersionSearchParams) {
  return request<Api.Cch.ChallengeVersionList>({
    url: '/cch/challengeVersion/list',
    method: 'get',
    params
  });
}

/** 新增题目版本 */
export function fetchCreateChallengeVersion(data: Api.Cch.ChallengeVersionOperateParams) {
  return request<boolean>({
    url: '/cch/challengeVersion',
    method: 'post',
    data
  });
}

/** 修改题目版本 */
export function fetchUpdateChallengeVersion(data: Api.Cch.ChallengeVersionOperateParams) {
  return request<boolean>({
    url: '/cch/challengeVersion',
    method: 'put',
    data
  });
}

/** 批量删除题目版本 */
export function fetchBatchDeleteChallengeVersion(ids: CommonType.IdType[]) {
  return request<boolean>({
    url: `/cch/challengeVersion/${ids.join(',')}`,
    method: 'delete'
  });
}
