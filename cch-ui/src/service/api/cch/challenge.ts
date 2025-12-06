import {request} from '@/service/request';

/** 获取题目列表列表 */
export function fetchGetChallengeList(params?: Api.Cch.ChallengeSearchParams) {
  return request<Api.Cch.ChallengeList>({
    url: '/cch/challenge/list',
    method: 'get',
    params
  });
}

/** 新增题目列表 */
export function fetchCreateChallenge(data: Api.Cch.ChallengeOperateParams) {
  return request<boolean>({
    url: '/cch/challenge',
    method: 'post',
    data
  });
}

/** 修改题目列表 */
export function fetchUpdateChallenge(data: Api.Cch.ChallengeOperateParams) {
  return request<boolean>({
    url: '/cch/challenge',
    method: 'put',
    data
  });
}

/** 批量删除题目列表 */
export function fetchBatchDeleteChallenge(ids: CommonType.IdType[]) {
  return request<boolean>({
    url: `/cch/challenge/${ids.join(',')}`,
    method: 'delete'
  });
}
