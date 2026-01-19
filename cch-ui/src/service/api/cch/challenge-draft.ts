import {request} from '@/service/request';

/** 获取题目草稿列表 */
export function fetchGetChallengeDraftList(params?: Api.Cch.ChallengeDraftSearchParams) {
  return request<Api.Cch.ChallengeDraftList>({
    url: '/cch/challengeDraft/list',
    method: 'get',
    params
  });
}

/** 新增题目草稿 */
export function fetchCreateChallengeDraft(data: Api.Cch.ChallengeDraftOperateParams) {
  return request<boolean>({
    url: '/cch/challengeDraft',
    method: 'post',
    data
  });
}

/** 修改题目草稿 */
export function fetchUpdateChallengeDraft(data: Api.Cch.ChallengeDraftOperateParams) {
  return request<Api.Cch.ChallengeDraft>({
    url: '/cch/challengeDraft',
    method: 'put',
    data
  });
}

/** 批量删除题目草稿 */
export function fetchBatchDeleteChallengeDraft(ids: CommonType.IdType[]) {
  return request<boolean>({
    url: `/cch/challengeDraft/${ids.join(',')}`,
    method: 'delete'
  });
}

/** 根据ID获取题目草稿 */
export function fetchGetChallengeDraftById(id: CommonType.IdType) {
  return request<Api.Cch.ChallengeDraft>({
    url: `/cch/challengeDraft/${id}`,
    method: 'get'
  });
}
