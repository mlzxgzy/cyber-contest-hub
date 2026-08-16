import {request} from '@/service/request';

/** 获取题目列表列表 */
export function fetchGetChallengeList(params?: Api.Cch.ChallengeSearchParams) {
    return request<Api.Cch.ChallengeList>({
        url: '/cch/challenge/list',
        method: 'get',
        params
    });
}

/** 获取题目列表列表 */
export function fetchGetChallengeById(challengeId: CommonType.IdType) {
    return request<Api.Cch.Challenge>({
        url: '/cch/challenge/' + challengeId,
        method: 'get',
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

/** 初始化一道新题目（含首个草稿），返回草稿信息 */
export function fetchInitChallenge(data: Api.Cch.ChallengeDraftOperateParams) {
    return request<Api.Cch.ChallengeDraft>({
        url: '/cch/challenge/init',
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

/** 获取题目的草稿信息 */
export function fetchChallengeDraftByChallengeId(challengeId: CommonType.IdType) {
    return request<Api.Cch.ChallengeDraft>({
        url: `/cch/challenge/${challengeId}/draft`,
        method: 'get'
    });
}
