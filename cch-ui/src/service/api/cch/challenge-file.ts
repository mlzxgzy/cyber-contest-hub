import { request } from '@/service/request';

/** 获取题目文件列表 */
export function fetchGetChallengeFileList (params?: Api.Cch.ChallengeFileSearchParams) {
    return request<Api.Cch.ChallengeFileList>({
        url: '/cch/challengeFile/list',
        method: 'get',
        params
    });
}
/** 新增题目文件 */
export function fetchCreateChallengeFile (data: Api.Cch.ChallengeFileOperateParams) {
    return request<boolean>({
        url: '/cch/challengeFile',
        method: 'post',
        data
    });
}

/** 修改题目文件 */
export function fetchUpdateChallengeFile (data: Api.Cch.ChallengeFileOperateParams) {
    return request<boolean>({
        url: '/cch/challengeFile',
        method: 'put',
        data
    });
}

/** 批量删除题目文件 */
export function fetchBatchDeleteChallengeFile (ids: CommonType.IdType[]) {
    return request<boolean>({
        url: `/cch/challengeFile/${ids.join(',')}`,
        method: 'delete'
    });
}
