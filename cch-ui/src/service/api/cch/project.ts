import {request} from '@/service/request';

/** 获取项目列表 */
export function fetchGetProjectList(params?: Api.Cch.ProjectSearchParams) {
    return request<Api.Cch.ProjectList>({
        url: '/cch/project/list',
        method: 'get',
        params
    });
}

/** 获取项目详情 */
export function fetchGetProjectDetail(projectId: CommonType.IdType) {
    return request<Api.Cch.Project>({
        url: `/cch/project/${projectId}`,
        method: 'get'
    });
}

/** 新增项目（返回新项目ID，超长整型已由后端序列化为字符串） */
export function fetchCreateProject(data: Api.Cch.ProjectOperateParams) {
    return request<CommonType.IdType>({
        url: '/cch/project',
        method: 'post',
        data
    });
}

/** 更新项目 */
export function fetchUpdateProject(data: Api.Cch.ProjectOperateParams) {
    return request<boolean>({
        url: '/cch/project',
        method: 'put',
        data
    });
}

/** 删除项目 */
export function fetchDeleteProject(ids: CommonType.IdType[]) {
    return request<boolean>({
        url: `/cch/project/${ids.join(',')}`,
        method: 'delete'
    });
}

/** 查询项目成员列表 */
export function fetchGetProjectMembers(projectId: CommonType.IdType) {
    return request<Api.Cch.ProjectMember[]>({
        url: `/cch/project/${projectId}/members`,
        method: 'get'
    });
}

/** 添加项目成员 */
export function fetchAddProjectMembers(projectId: CommonType.IdType, members: Array<{userId: CommonType.IdType; permissionType: 'admin' | 'view_all' | 'view_own'}>) {
    return request<boolean>({
        url: `/cch/project/${projectId}/members`,
        method: 'post',
        data: members
    });
}

/** 移除项目成员 */
export function fetchRemoveProjectMembers(projectId: CommonType.IdType, userIds: CommonType.IdType[]) {
    return request<boolean>({
        url: `/cch/project/${projectId}/members`,
        method: 'delete',
        data: userIds
    });
}

/** 生成项目成员邀请Code */
export function fetchGenerateProjectInviteCode(
    projectId: CommonType.IdType,
    permissionType: 'admin' | 'view_all' | 'view_own'
) {
    return request<string>({
        url: `/cch/project/${projectId}/members/invite-code`,
        method: 'post',
        data: { permissionType }
    });
}

/** 通过邀请Code加入项目 */
export function fetchJoinProjectByInvite(projectId: CommonType.IdType, inviteCode: string) {
    return request<boolean>({
        url: `/cch/project/${projectId}/members/join-by-invite`,
        method: 'post',
        data: { inviteCode }
    });
}

/** 查询项目题目列表 */
export function fetchGetProjectChallenges(projectId: CommonType.IdType) {
    return request<Api.Cch.ProjectChallenge[]>({
        url: `/cch/project/${projectId}/challenges`,
        method: 'get'
    });
}

/** 导入项目题目 */
export function fetchImportProjectChallenges(projectId: CommonType.IdType, challenges: Array<{versionId: CommonType.IdType}>) {
    return request<boolean>({
        url: `/cch/project/${projectId}/challenges`,
        method: 'post',
        data: challenges
    });
}

/** 移除项目题目 */
export function fetchRemoveProjectChallenges(projectId: CommonType.IdType, challengeIds: CommonType.IdType[]) {
    return request<boolean>({
        url: `/cch/project/${projectId}/challenges`,
        method: 'delete',
        data: challengeIds
    });
}

/** 上传竞赛文件 */
export function fetchUploadContestFile(projectId: CommonType.IdType, file: File, fileTag?: string) {
    const formData = new FormData();
    formData.append('file', file);
    if (fileTag) {
        formData.append('fileTag', fileTag);
    }
    return request<Api.Cch.ContestFile>({
        url: `/cch/project/${projectId}/files/upload`,
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}

/** 下载竞赛文件 */
export function fetchDownloadContestFile(fileId: CommonType.IdType) {
    return request<Blob>({
        url: `/cch/project/files/${fileId}/download`,
        method: 'get',
        responseType: 'blob'
    });
}

/** 删除竞赛文件 */
export function fetchRemoveContestFile(fileIds: CommonType.IdType[]) {
    return request<boolean>({
        url: `/cch/project/files/${fileIds.join(',')}`,
        method: 'delete'
    });
}
