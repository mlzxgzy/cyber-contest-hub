package com.kdajv.cch.service;

import com.kdajv.cch.domain.bo.ContestFileBo;
import com.kdajv.cch.domain.bo.ProjectBo;
import com.kdajv.cch.domain.bo.ProjectChallengeBo;
import com.kdajv.cch.domain.bo.ProjectMemberBo;
import com.kdajv.cch.domain.vo.ContestFileVo;
import com.kdajv.cch.domain.vo.ProjectChallengeVo;
import com.kdajv.cch.domain.vo.ProjectMemberVo;
import com.kdajv.cch.domain.vo.ProjectVo;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * 项目Service接口
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
public interface IProjectService {

    /**
     * 分页查询项目列表（支持类型筛选）
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 项目分页列表
     */
    TableDataInfo<ProjectVo> queryPageList(ProjectBo bo, PageQuery pageQuery);

    /**
     * 查询项目详情（包含成员、题目、文件）
     *
     * @param id 项目ID
     * @return 项目详情
     */
    ProjectVo queryById(Long id);

    /**
     * 新增项目（普通/竞赛），创建者自动成为管理员
     *
     * @param bo 项目信息
     * @return 是否新增成功
     */
    Boolean insertByBo(ProjectBo bo);

    /**
     * 更新项目
     *
     * @param bo 项目信息
     * @return 是否更新成功
     */
    Boolean updateByBo(ProjectBo bo);

    /**
     * 删除项目（只删除项目本身，不级联删除关联数据）
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 添加项目成员（需要项目管理员权限）
     *
     * @param projectId 项目ID
     * @param members   成员列表
     * @return 是否添加成功
     */
    Boolean addMembers(Long projectId, List<ProjectMemberBo> members);

    /**
     * 移除项目成员（需要项目管理员权限）
     *
     * @param projectId 项目ID
     * @param userIds   用户ID列表
     * @return 是否移除成功
     */
    Boolean removeMembers(Long projectId, List<Long> userIds);

    /**
     * 生成项目成员邀请Code（需要项目管理员权限）
     *
     * @param projectId      项目ID
     * @param permissionType 权限类型
     * @return 邀请Code
     */
    String generateInviteCode(Long projectId, String permissionType);

    /**
     * 通过邀请Code加入项目（仅需登录，无需原项目权限）
     *
     * @param projectId  项目ID
     * @param inviteCode 邀请Code
     * @return 是否加入成功
     */
    Boolean joinByInvite(Long projectId, String inviteCode);

    /**
     * 查询项目题目列表（根据当前用户权限过滤）
     *
     * @param projectId 项目ID
     * @return 题目列表
     */
    List<ProjectChallengeVo> queryProjectChallenges(Long projectId);

    /**
     * 导入题目（验证version_id在t_challenge_version表中存在，需要项目管理员权限）
     *
     * @param projectId  项目ID
     * @param challenges 题目列表
     * @return 是否导入成功
     */
    Boolean importChallenges(Long projectId, List<ProjectChallengeBo> challenges);

    /**
     * 移除题目（需要项目管理员权限）
     *
     * @param projectId   项目ID
     * @param challengeIds 题目关联ID列表
     * @return 是否移除成功
     */
    Boolean removeChallenges(Long projectId, List<Long> challengeIds);

    /**
     * 上传竞赛文件（验证项目类型是contest，需要项目管理员权限）
     *
     * @param projectId 项目ID
     * @param file      文件
     * @param fileTag   文件标签
     * @return 文件信息
     */
    ContestFileVo uploadContestFile(Long projectId, MultipartFile file, String fileTag);

    /**
     * 下载竞赛文件（需要项目成员权限）
     *
     * @param fileId   文件ID
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void downloadContestFile(Long fileId, HttpServletResponse response) throws IOException;

    /**
     * 删除竞赛文件（需要项目管理员权限，只删除ContestFile记录，不删除OSS文件）
     *
     * @param fileIds 文件ID列表
     * @return 是否删除成功
     */
    Boolean removeContestFile(List<Long> fileIds);
}
