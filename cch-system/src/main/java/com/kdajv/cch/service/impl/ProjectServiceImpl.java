package com.kdajv.cch.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kdajv.cch.domain.*;
import com.kdajv.cch.enums.ChallengeStatus;
import com.kdajv.cch.domain.bo.ProjectBo;
import com.kdajv.cch.domain.bo.ProjectChallengeBo;
import com.kdajv.cch.domain.bo.ProjectMemberBo;
import com.kdajv.cch.domain.vo.ContestFileVo;
import com.kdajv.cch.domain.vo.ProjectChallengeVo;
import com.kdajv.cch.domain.vo.ProjectMemberVo;
import com.kdajv.cch.domain.vo.ProjectVo;
import com.kdajv.cch.mapper.*;
import com.kdajv.cch.service.IProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.service.ISysConfigService;
import org.dromara.system.service.ISysOssService;
import org.dromara.system.service.ISysUserService;
import com.kdajv.cch.service.IChallengeVersionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目Service业务层处理
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectServiceImpl implements IProjectService {

    private final ProjectMapper baseMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectChallengeMapper projectChallengeMapper;
    private final ContestFileMapper contestFileMapper;
    private final ProjectMemberInviteMapper projectMemberInviteMapper;
    private final ISysUserService sysUserService;
    private final ISysOssService sysOssService;
    private final IChallengeVersionService challengeVersionService;
    private final ChallengeMapper challengeMapper;
    private final ISysConfigService sysConfigService;

    /**
     * 分页查询项目列表（支持类型筛选）
     * <p>
     * 可见性过滤：超管可见全部项目；普通用户仅可见自己创建或作为成员（含邀请加入）的项目。
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 项目分页列表
     */
    @Override
    public TableDataInfo<ProjectVo> queryPageList(ProjectBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Project> lqw = buildQueryWrapper(bo);
        applyVisibleFilter(lqw);
        Page<ProjectVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 应用项目可见性过滤：超管可见全部；普通用户仅可见自己创建或作为成员的项目
     */
    private void applyVisibleFilter(LambdaQueryWrapper<Project> lqw) {
        Long currentUserId = LoginHelper.getUserId();
        if (ObjectUtil.isNull(currentUserId)) {
            throw new ServiceException("用户未登录");
        }
        // 超管可见全部
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        // 创建者 或 项目成员（含邀请加入）
        lqw.and(w -> w
            .eq(Project::getCreateBy, currentUserId)
            .or()
            .apply("id in (select project_id from t_project_member where user_id = {0} and del_flag = 0)", currentUserId)
        );
    }

    /**
     * 校验项目可见性：超管可见全部；普通用户仅可见自己创建或作为成员（含邀请加入）的项目
     *
     * @param projectVo 项目信息
     */
    private void checkProjectVisible(ProjectVo projectVo) {
        Long currentUserId = LoginHelper.getUserId();
        if (ObjectUtil.isNull(currentUserId)) {
            throw new ServiceException("用户未登录");
        }
        // 超管可见全部
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        // 创建者可见
        if (Objects.equals(projectVo.getCreateBy(), currentUserId)) {
            return;
        }
        // 成员可见（含邀请加入）
        ProjectMember member = projectMemberMapper.selectOne(
            new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectVo.getId())
                .eq(ProjectMember::getUserId, currentUserId)
        );
        if (ObjectUtil.isNull(member)) {
            throw new ServiceException("您不是该项目的成员，无权查看该项目");
        }
    }

    /**
     * 查询项目详情（包含成员、题目、文件）
     * <p>
     * 可见性校验：超管可见全部；普通用户仅可见自己创建或作为成员（含邀请加入）的项目。
     *
     * @param id 项目ID
     * @return 项目详情
     */
    @Override
    public ProjectVo queryById(Long id) {
        ProjectVo projectVo = baseMapper.selectVoById(id);
        if (ObjectUtil.isNull(projectVo)) {
            return null;
        }

        // 可见性校验：超管可见全部；普通用户仅可见自己创建或作为成员的项目
        checkProjectVisible(projectVo);

        // 查询项目成员
        List<ProjectMemberVo> members = projectMemberMapper.selectVoList(
            new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, id)
        );
        // 填充用户信息
        fillMemberUserInfo(members);
        projectVo.setMembers(members);

        // 查询项目题目
        List<ProjectChallengeVo> challenges = queryProjectChallenges(id);
        projectVo.setChallenges(challenges);

        // 如果是竞赛项目，查询竞赛文件
        if ("contest".equals(projectVo.getProjectType())) {
            List<ContestFileVo> contestFiles = contestFileMapper.selectVoList(
                new LambdaQueryWrapper<ContestFile>()
                    .eq(ContestFile::getProjectId, id)
            );
            // 填充OSS文件信息
            fillContestFileOssInfo(contestFiles);
            projectVo.setContestFiles(contestFiles);
        }

        return projectVo;
    }

    /**
     * 新增项目（普通/竞赛），创建者自动成为管理员
     *
     * @param bo 项目信息
     * @return 新增成功返回项目ID，失败返回 null
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertByBo(ProjectBo bo) {
        Long currentUserId = LoginHelper.getUserId();
        if (ObjectUtil.isNull(currentUserId)) {
            throw new ServiceException("用户未登录");
        }

        Project project = MapstructUtils.convert(bo, Project.class);
        boolean result = baseMapper.insert(project) > 0;
        if (!result) {
            return null;
        }

        Long projectId = project.getId();

        // 创建者自动成为管理员
        ProjectMember adminMember = new ProjectMember();
        adminMember.setProjectId(projectId);
        adminMember.setUserId(currentUserId);
        adminMember.setPermissionType("admin");
        projectMemberMapper.insert(adminMember);

        // 批量添加成员
        if (CollUtil.isNotEmpty(bo.getMembers())) {
            addMembers(projectId, bo.getMembers());
        }

        // 批量导入题目
        if (CollUtil.isNotEmpty(bo.getChallenges())) {
            importChallenges(projectId, bo.getChallenges());
        }

        return projectId;
    }

    /**
     * 更新项目
     *
     * @param bo 项目信息
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(ProjectBo bo) {
        // 检查项目是否存在
        ProjectVo existingProject = baseMapper.selectVoById(bo.getId());
        if (ObjectUtil.isNull(existingProject)) {
            throw new ServiceException("项目不存在");
        }

        // 检查管理员权限
        checkAdminPermission(bo.getId());

        // 使用 LambdaUpdateWrapper 明确指定要更新的字段，避免数据拼接问题
        LambdaUpdateWrapper<Project> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Project::getId, bo.getId());
        
        if (StringUtils.isNotBlank(bo.getProjectType())) {
            updateWrapper.set(Project::getProjectType, bo.getProjectType());
        }
        if (StringUtils.isNotBlank(bo.getName())) {
            updateWrapper.set(Project::getName, bo.getName());
        }
        // 允许将备注清空为空字符串（行内编辑场景）
        if (bo.getRemark() != null) {
            updateWrapper.set(Project::getRemark, bo.getRemark());
        }
        // 允许将负责人清空为空字符串（行内编辑场景）
        if (bo.getLeader() != null) {
            updateWrapper.set(Project::getLeader, bo.getLeader());
        }
        
        // 处理 meta 字段
        // 注意：LambdaUpdateWrapper.set 不会自动应用实体字段上的 JacksonTypeHandler，
        // 直接传入 ContestMeta 对象会被 MyBatis 用 Java 默认序列化（产生 \xAC\xED 魔数），
        // 导致 meta 列 json_valid 校验失败。因此这里显式将 meta 序列化为 JSON 字符串后存储。
        if (bo.getMeta() != null) {
            updateWrapper.set(Project::getMeta, JsonUtils.toJsonString(bo.getMeta()));
        } else if (bo.getMeta() == null && "contest".equals(bo.getProjectType())) {
            // 如果是竞赛项目但没有提供 meta，保持原有 meta 不变
            // 这里不设置 meta 字段，让它保持原值
        } else if ("normal".equals(bo.getProjectType())) {
            // 如果是普通项目，清除 meta 字段
            updateWrapper.set(Project::getMeta, null);
        }

        return baseMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 删除项目（只删除项目本身，不级联删除关联数据）
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }

        // 有效性校验
        if (Boolean.TRUE.equals(isValid)) {
            for (Long id : ids) {
                checkAdminPermission(id);
            }
        }

        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 添加项目成员（需要项目管理员权限）
     *
     * @param projectId 项目ID
     * @param members   成员列表
     * @return 是否添加成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addMembers(Long projectId, List<ProjectMemberBo> members) {
        if (CollUtil.isEmpty(members)) {
            return true;
        }

        // 检查管理员权限
        checkAdminPermission(projectId);

        // 检查项目是否存在
        ProjectVo project = baseMapper.selectVoById(projectId);
        if (ObjectUtil.isNull(project)) {
            throw new ServiceException("项目不存在");
        }

        for (ProjectMemberBo memberBo : members) {
            // 检查用户是否存在
            SysUserVo user = sysUserService.selectUserById(memberBo.getUserId());
            if (ObjectUtil.isNull(user)) {
                throw new ServiceException("用户不存在，用户ID: " + memberBo.getUserId());
            }

            // 检查是否已经是成员
            ProjectMember existingMember = projectMemberMapper.selectOne(
                new LambdaQueryWrapper<ProjectMember>()
                    .eq(ProjectMember::getProjectId, projectId)
                    .eq(ProjectMember::getUserId, memberBo.getUserId())
            );

            if (ObjectUtil.isNotNull(existingMember)) {
                // 更新权限类型
                existingMember.setPermissionType(memberBo.getPermissionType());
                projectMemberMapper.updateById(existingMember);
            } else {
                // 新增成员
                ProjectMember member = MapstructUtils.convert(memberBo, ProjectMember.class);
                member.setProjectId(projectId);
                projectMemberMapper.insert(member);
            }
        }

        return true;
    }

    /**
     * 移除项目成员（需要项目管理员权限）
     *
     * @param projectId 项目ID
     * @param userIds   用户ID列表
     * @return 是否移除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeMembers(Long projectId, List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return true;
        }

        // 检查管理员权限
        checkAdminPermission(projectId);

        // 不能移除最后一个管理员
        List<ProjectMember> adminMembers = projectMemberMapper.selectList(
            new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getPermissionType, "admin")
        );

        // 检查要移除的用户中是否包含管理员
        Set<Long> userIdSet = new HashSet<>(userIds);
        long adminCount = adminMembers.stream()
            .filter(m -> userIdSet.contains(m.getUserId()))
            .count();

        if (adminCount > 0 && adminMembers.size() <= adminCount) {
            throw new ServiceException("不能移除最后一个管理员");
        }

        return projectMemberMapper.delete(
            new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .in(ProjectMember::getUserId, userIds)
        ) > 0;
    }

    /**
     * 生成项目成员邀请Code（需要项目管理员权限）
     *
     * @param projectId      项目ID
     * @param permissionType 权限类型
     * @return 邀请Code
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateInviteCode(Long projectId, String permissionType) {
        // 检查管理员权限
        checkAdminPermission(projectId);

        // 校验项目是否存在
        ProjectVo project = baseMapper.selectVoById(projectId);
        if (ObjectUtil.isNull(project)) {
            throw new ServiceException("项目不存在");
        }

        // 校验权限类型
        if (!Objects.equals("admin", permissionType)
            && !Objects.equals("view_all", permissionType)
            && !Objects.equals("view_own", permissionType)) {
            throw new ServiceException("无效的权限类型");
        }

        // 从sys_config读取有效期（单位：分钟），不存在时使用默认值
        final String configKey = "cch.project.invite.expireMinutes";
        String expireMinutesStr = sysConfigService.selectConfigByKey(configKey);
        long expireMinutes = 60L;
        if (StringUtils.isNotBlank(expireMinutesStr)) {
            try {
                expireMinutes = Long.parseLong(expireMinutesStr);
            } catch (NumberFormatException e) {
                log.warn("解析项目成员邀请有效期配置失败，使用默认值60分钟, configKey={}, value={}", configKey, expireMinutesStr);
            }
        }
        if (expireMinutes <= 0) {
            expireMinutes = 60L;
        }

        Date expireTime = Date.from(
            java.time.LocalDateTime.now()
                .plusMinutes(expireMinutes)
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
        );

        // 生成随机邀请Code
        String inviteCode = java.util.UUID.randomUUID().toString().replace("-", "");

        ProjectMemberInvite invite = new ProjectMemberInvite();
        invite.setProjectId(projectId);
        invite.setPermissionType(permissionType);
        invite.setInviteCode(inviteCode);
        invite.setExpireTime(expireTime);

        projectMemberInviteMapper.insert(invite);

        return inviteCode;
    }

    /**
     * 通过邀请Code加入项目（仅需登录，无需原项目权限）
     *
     * @param projectId  项目ID
     * @param inviteCode 邀请Code
     * @return 是否加入成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean joinByInvite(Long projectId, String inviteCode) {
        Long currentUserId = LoginHelper.getUserId();
        if (ObjectUtil.isNull(currentUserId)) {
            throw new ServiceException("用户未登录");
        }

        if (StringUtils.isBlank(inviteCode)) {
            throw new ServiceException("邀请Code不能为空");
        }

        // 校验项目是否存在
        ProjectVo project = baseMapper.selectVoById(projectId);
        if (ObjectUtil.isNull(project)) {
            throw new ServiceException("项目不存在");
        }

        // 查询有效的邀请记录
        ProjectMemberInvite invite = projectMemberInviteMapper.selectOne(
            new LambdaQueryWrapper<ProjectMemberInvite>()
                .eq(ProjectMemberInvite::getProjectId, projectId)
                .eq(ProjectMemberInvite::getInviteCode, inviteCode)
        );

        if (ObjectUtil.isNull(invite)) {
            throw new ServiceException("邀请链接无效");
        }

        if (invite.getExpireTime() != null && invite.getExpireTime().before(new Date())) {
            throw new ServiceException("邀请链接已过期");
        }

        // 检查用户是否已经是成员
        ProjectMember existingMember = projectMemberMapper.selectOne(
            new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, currentUserId)
        );

        if (ObjectUtil.isNotNull(existingMember)) {
            // 已经是成员，则不重复添加，直接返回成功（不消耗邀请链接）
            return true;
        }

        // 校验权限类型
        String permissionType = invite.getPermissionType();
        if (!Objects.equals("admin", permissionType)
            && !Objects.equals("view_all", permissionType)
            && !Objects.equals("view_own", permissionType)) {
            throw new ServiceException("邀请链接中的权限类型无效");
        }

        // 新增成员
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(currentUserId);
        member.setPermissionType(permissionType);
        projectMemberMapper.insert(member);

        // 单次邀请：成功加入后立即作废该邀请记录
        projectMemberInviteMapper.deleteById(invite.getId());

        return true;
    }

    /**
     * 查询项目题目列表（根据当前用户权限过滤）
     *
     * @param projectId 项目ID
     * @return 题目列表
     */
    @Override
    public List<ProjectChallengeVo> queryProjectChallenges(Long projectId) {
        Long currentUserId = LoginHelper.getUserId();
        if (ObjectUtil.isNull(currentUserId)) {
            throw new ServiceException("用户未登录");
        }

        // 检查用户是否是项目成员（超管默认放行）
        ProjectMember member = null;
        if (!LoginHelper.isSuperAdmin()) {
            member = projectMemberMapper.selectOne(
                new LambdaQueryWrapper<ProjectMember>()
                    .eq(ProjectMember::getProjectId, projectId)
                    .eq(ProjectMember::getUserId, currentUserId)
            );

            if (ObjectUtil.isNull(member)) {
                throw new ServiceException("您不是该项目的成员");
            }
        }

        LambdaQueryWrapper<ProjectChallenge> queryWrapper = new LambdaQueryWrapper<ProjectChallenge>()
            .eq(ProjectChallenge::getProjectId, projectId);

        // 根据权限类型过滤（超管查看全部，无需过滤）
        if (member != null) {
            String permissionType = member.getPermissionType();
            if ("view_own".equals(permissionType)) {
                // 仅查看自己导入的题目
                queryWrapper.eq(ProjectChallenge::getCreateBy, currentUserId);
            }
            // admin 和 view_all 可以查看所有题目，不需要额外过滤
        }

        List<ProjectChallengeVo> challenges = projectChallengeMapper.selectVoList(queryWrapper);

        // 填充题目名称和版本号信息
        fillChallengeInfo(challenges);

        return challenges;
    }

    /**
     * 查询题目附加到的项目列表（挑战侧反向查看，按题目查所有版本的附加记录）
     * <p>
     * 可见性过滤：超管返回全部附加记录；普通用户仅返回自己创建或作为成员（含邀请加入）的项目中的附加记录，
     * 避免通过该接口越权探测非可见项目的关联信息。
     *
     * @param challengeId 题目ID
     * @return 附加记录列表（含项目名称）
     */
    @Override
    public List<ProjectChallengeVo> queryProjectChallengesByChallengeId(Long challengeId) {
        if (ObjectUtil.isNull(challengeId)) {
            throw new ServiceException("题目ID不能为空");
        }

        Long currentUserId = LoginHelper.getUserId();
        if (ObjectUtil.isNull(currentUserId)) {
            throw new ServiceException("用户未登录");
        }

        LambdaQueryWrapper<ProjectChallenge> queryWrapper = new LambdaQueryWrapper<ProjectChallenge>()
            .eq(ProjectChallenge::getChallengeId, challengeId)
            .orderByDesc(ProjectChallenge::getCreateTime);

        // 超管返回全部；普通用户仅返回自己创建或作为成员的项目中的附加记录
        if (!LoginHelper.isSuperAdmin()) {
            queryWrapper.and(w -> w
                .apply("project_id in (select id from t_project where create_by = {0})", currentUserId)
                .or()
                .apply("project_id in (select project_id from t_project_member where user_id = {0} and del_flag = 0)", currentUserId)
            );
        }

        List<ProjectChallengeVo> challenges = projectChallengeMapper.selectVoList(queryWrapper);

        // 填充项目名称信息
        fillProjectInfo(challenges);
        // 填充题目名称和版本号信息
        fillChallengeInfo(challenges);
        // 填充创建人名称信息
        fillChallengeCreatorInfo(challenges);

        return challenges;
    }

    /**
     * 导入题目（验证version_id在t_challenge_version表中存在，需要项目管理员权限）
     *
     * @param projectId  项目ID
     * @param challenges 题目列表
     * @return 是否导入成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean importChallenges(Long projectId, List<ProjectChallengeBo> challenges) {
        if (CollUtil.isEmpty(challenges)) {
            return true;
        }

        // 检查管理员权限
        checkAdminPermission(projectId);

        // 检查项目是否存在
        ProjectVo project = baseMapper.selectVoById(projectId);
        if (ObjectUtil.isNull(project)) {
            throw new ServiceException("项目不存在");
        }

        Long currentUserId = LoginHelper.getUserId();

        for (ProjectChallengeBo challengeBo : challenges) {
            // 验证version_id是否存在
            var versionVo = challengeVersionService.queryById(challengeBo.getVersionId());
            if (ObjectUtil.isNull(versionVo)) {
                throw new ServiceException("题目版本不存在，版本ID: " + challengeBo.getVersionId());
            }

            // 停用的题目不允许导入项目
            Challenge challenge = challengeMapper.selectById(versionVo.getChallengeId());
            if (ObjectUtil.isNotNull(challenge) && ChallengeStatus.DISABLED.getCode() == challenge.getStatus()) {
                throw new ServiceException(String.format("题目「%s」已停用，不允许导入项目", challenge.getName()));
            }

            // 检查是否已经导入
            ProjectChallenge existingChallenge = projectChallengeMapper.selectOne(
                new LambdaQueryWrapper<ProjectChallenge>()
                    .eq(ProjectChallenge::getProjectId, projectId)
                    .eq(ProjectChallenge::getVersionId, challengeBo.getVersionId())
            );

            if (ObjectUtil.isNotNull(existingChallenge)) {
                // 已存在，跳过
                continue;
            }

            // 新增项目题目关联
            ProjectChallenge projectChallenge = MapstructUtils.convert(challengeBo, ProjectChallenge.class);
            projectChallenge.setProjectId(projectId);
            projectChallenge.setChallengeId(versionVo.getChallengeId());
            projectChallengeMapper.insert(projectChallenge);
        }

        return true;
    }

    /**
     * 移除题目（需要项目管理员权限）
     *
     * @param projectId    项目ID
     * @param challengeIds 题目关联ID列表
     * @return 是否移除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeChallenges(Long projectId, List<Long> challengeIds) {
        if (CollUtil.isEmpty(challengeIds)) {
            return true;
        }

        // 检查管理员权限
        checkAdminPermission(projectId);

        return projectChallengeMapper.delete(
            new LambdaQueryWrapper<ProjectChallenge>()
                .eq(ProjectChallenge::getProjectId, projectId)
                .in(ProjectChallenge::getId, challengeIds)
        ) > 0;
    }

    /**
     * 上传竞赛文件（验证项目类型是contest，需要项目管理员权限）
     *
     * @param projectId 项目ID
     * @param file      文件
     * @param fileTag   文件标签
     * @return 文件信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContestFileVo uploadContestFile(Long projectId, MultipartFile file, String fileTag) {
        // 检查管理员权限
        checkAdminPermission(projectId);

        // 检查项目是否存在且类型为contest
        ProjectVo project = baseMapper.selectVoById(projectId);
        if (ObjectUtil.isNull(project)) {
            throw new ServiceException("项目不存在");
        }
        if (!"contest".equals(project.getProjectType())) {
            throw new ServiceException("只有竞赛项目才能上传文件");
        }

        // 上传文件到OSS
        SysOssVo ossVo = sysOssService.upload(file);

        // 保存文件记录
        ContestFile contestFile = new ContestFile();
        contestFile.setProjectId(projectId);
        contestFile.setOssId(ossVo.getOssId());
        contestFile.setFileTag(fileTag);
        contestFileMapper.insert(contestFile);

        // 转换为VO并填充OSS信息
        ContestFileVo fileVo = MapstructUtils.convert(contestFile, ContestFileVo.class);
        fileVo.setFileName(ossVo.getFileName());
        fileVo.setOriginalName(ossVo.getOriginalName());
        fileVo.setUrl(ossVo.getUrl());

        return fileVo;
    }

    /**
     * 下载竞赛文件（需要项目成员权限）
     *
     * @param fileId   文件ID
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    @Override
    public void downloadContestFile(Long fileId, HttpServletResponse response) throws IOException {
        Long currentUserId = LoginHelper.getUserId();
        if (ObjectUtil.isNull(currentUserId)) {
            throw new ServiceException("用户未登录");
        }

        // 查询文件信息
        ContestFileVo fileVo = contestFileMapper.selectVoById(fileId);
        if (ObjectUtil.isNull(fileVo)) {
            throw new ServiceException("文件不存在");
        }

        // 检查用户是否是项目成员
        ProjectMember member = projectMemberMapper.selectOne(
            new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, fileVo.getProjectId())
                .eq(ProjectMember::getUserId, currentUserId)
        );

        if (ObjectUtil.isNull(member)) {
            throw new ServiceException("您不是该项目的成员，无权下载文件");
        }

        // 通过OSS服务下载文件
        sysOssService.download(fileVo.getOssId(), response);
    }

    /**
     * 删除竞赛文件（需要项目管理员权限，只删除ContestFile记录，不删除OSS文件）
     *
     * @param fileIds 文件ID列表
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeContestFile(List<Long> fileIds) {
        if (CollUtil.isEmpty(fileIds)) {
            return true;
        }

        // 查询文件信息，检查管理员权限
        List<ContestFileVo> files = contestFileMapper.selectVoList(
            new LambdaQueryWrapper<ContestFile>()
                .in(ContestFile::getId, fileIds)
        );

        if (CollUtil.isEmpty(files)) {
            return true;
        }

        // 检查每个文件所属项目的管理员权限
        Set<Long> projectIds = files.stream()
            .map(ContestFileVo::getProjectId)
            .collect(Collectors.toSet());

        for (Long projectId : projectIds) {
            checkAdminPermission(projectId);
        }

        // 只删除ContestFile记录，不删除OSS文件
        return contestFileMapper.deleteBatchIds(fileIds) > 0;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Project> buildQueryWrapper(ProjectBo bo) {
        LambdaQueryWrapper<Project> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ObjectUtil.isNotNull(bo.getId()), Project::getId, bo.getId());
        lqw.eq(StringUtils.isNotBlank(bo.getProjectType()), Project::getProjectType, bo.getProjectType());
        lqw.like(StringUtils.isNotBlank(bo.getName()), Project::getName, bo.getName());
        lqw.orderByDesc(Project::getCreateTime);
        return lqw;
    }

    /**
     * 检查用户是否是项目管理员
     */
    private void checkAdminPermission(Long projectId) {
        Long currentUserId = LoginHelper.getUserId();
        if (ObjectUtil.isNull(currentUserId)) {
            throw new ServiceException("用户未登录");
        }

        // 超管默认允许
        if (LoginHelper.isSuperAdmin()) {
            return;
        }

        ProjectMember member = projectMemberMapper.selectOne(
            new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, currentUserId)
                .eq(ProjectMember::getPermissionType, "admin")
        );

        if (ObjectUtil.isNull(member)) {
            throw new ServiceException("您不是该项目的管理员，无权执行此操作");
        }
    }

    /**
     * 填充成员用户信息
     */
    private void fillMemberUserInfo(List<ProjectMemberVo> members) {
        if (CollUtil.isEmpty(members)) {
            return;
        }

        List<Long> userIds = members.stream()
            .map(ProjectMemberVo::getUserId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        if (CollUtil.isEmpty(userIds)) {
            return;
        }

        Map<Long, SysUserVo> userMap = new HashMap<>();
        for (Long userId : userIds) {
            SysUserVo user = sysUserService.selectUserById(userId);
            if (ObjectUtil.isNotNull(user)) {
                userMap.put(userId, user);
            }
        }

        for (ProjectMemberVo member : members) {
            SysUserVo user = userMap.get(member.getUserId());
            if (ObjectUtil.isNotNull(user)) {
                member.setUserName(user.getUserName());
                member.setNickName(user.getNickName());
            }
        }
    }

    /**
     * 填充项目名称信息
     */
    private void fillProjectInfo(List<ProjectChallengeVo> challenges) {
        if (CollUtil.isEmpty(challenges)) {
            return;
        }

        List<Long> projectIds = challenges.stream()
            .map(ProjectChallengeVo::getProjectId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        if (CollUtil.isEmpty(projectIds)) {
            return;
        }

        Map<Long, String> projectNameMap = new HashMap<>();
        List<ProjectVo> projects = baseMapper.selectVoByIds(projectIds);
        if (CollUtil.isNotEmpty(projects)) {
            for (ProjectVo project : projects) {
                if (ObjectUtil.isNotNull(project) && ObjectUtil.isNotNull(project.getId())) {
                    projectNameMap.put(project.getId(), project.getName());
                }
            }
        }

        for (ProjectChallengeVo challenge : challenges) {
            challenge.setProjectName(projectNameMap.get(challenge.getProjectId()));
        }
    }

    /**
     * 填充题目信息（题目名称和版本号）
     */
    private void fillChallengeInfo(List<ProjectChallengeVo> challenges) {
        if (CollUtil.isEmpty(challenges)) {
            return;
        }

        List<Long> versionIds = challenges.stream()
            .map(ProjectChallengeVo::getVersionId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        if (CollUtil.isEmpty(versionIds)) {
            return;
        }

        Map<Long, com.kdajv.cch.domain.vo.ChallengeVersionVo> versionMap = new HashMap<>();
        for (Long versionId : versionIds) {
            try {
                com.kdajv.cch.domain.vo.ChallengeVersionVo version = challengeVersionService.queryById(versionId);
                if (ObjectUtil.isNotNull(version)) {
                    versionMap.put(versionId, version);
                }
            } catch (Exception e) {
                log.warn("查询题目版本信息失败，版本ID: {}", versionId, e);
            }
        }

        for (ProjectChallengeVo challenge : challenges) {
            com.kdajv.cch.domain.vo.ChallengeVersionVo version = versionMap.get(challenge.getVersionId());
            if (ObjectUtil.isNotNull(version)) {
                challenge.setChallengeName(version.getChallengeName());
                challenge.setVersionTag(version.getVersionTag());
            }
        }
    }

    /**
     * 填充创建人名称信息
     */
    private void fillChallengeCreatorInfo(List<ProjectChallengeVo> challenges) {
        if (CollUtil.isEmpty(challenges)) {
            return;
        }

        List<Long> createByIds = challenges.stream()
            .map(ProjectChallengeVo::getCreateBy)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        if (CollUtil.isEmpty(createByIds)) {
            return;
        }

        Map<Long, SysUserVo> userMap = new HashMap<>();
        List<SysUserVo> users = sysUserService.selectUserByIds(createByIds, null);
        if (CollUtil.isNotEmpty(users)) {
            for (SysUserVo user : users) {
                if (ObjectUtil.isNotNull(user) && ObjectUtil.isNotNull(user.getUserId())) {
                    userMap.put(user.getUserId(), user);
                }
            }
        }

        for (ProjectChallengeVo challenge : challenges) {
            SysUserVo user = userMap.get(challenge.getCreateBy());
            if (ObjectUtil.isNotNull(user)) {
                challenge.setCreateByName(user.getNickName() != null ? user.getNickName() : user.getUserName());
            }
        }
    }

    /**
     * 填充竞赛文件OSS信息
     */
    private void fillContestFileOssInfo(List<ContestFileVo> files) {
        if (CollUtil.isEmpty(files)) {
            return;
        }

        List<Long> ossIds = files.stream()
            .map(ContestFileVo::getOssId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        if (CollUtil.isEmpty(ossIds)) {
            return;
        }

        List<SysOssVo> ossList = sysOssService.listByIds(ossIds);
        Map<Long, SysOssVo> ossMap = ossList.stream()
            .collect(Collectors.toMap(SysOssVo::getOssId, v -> v));

        for (ContestFileVo file : files) {
            SysOssVo oss = ossMap.get(file.getOssId());
            if (ObjectUtil.isNotNull(oss)) {
                file.setFileName(oss.getFileName());
                file.setOriginalName(oss.getOriginalName());
                file.setUrl(oss.getUrl());
            }
        }
    }
}
