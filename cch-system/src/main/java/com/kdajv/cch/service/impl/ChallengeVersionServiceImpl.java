package com.kdajv.cch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kdajv.cch.domain.Challenge;
import com.kdajv.cch.domain.ChallengeDraft;
import com.kdajv.cch.domain.ChallengeVersion;
import com.kdajv.cch.domain.DraftConfig;
import com.kdajv.cch.domain.bo.ChallengeVersionBo;
import com.kdajv.cch.domain.vo.ChallengeVersionVo;
import com.kdajv.cch.mapper.ChallengeDraftMapper;
import com.kdajv.cch.mapper.ChallengeMapper;
import com.kdajv.cch.mapper.ChallengeVersionMapper;
import com.kdajv.cch.service.IChallengeVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 题目版本Service业务层处理
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ChallengeVersionServiceImpl implements IChallengeVersionService {

    private final ChallengeVersionMapper baseMapper;
    private final ChallengeMapper challengeMapper;
    private final ChallengeDraftMapper challengeDraftMapper;

    /**
     * 查询题目版本
     *
     * @param id 主键
     * @return 题目版本
     */
    @Override
    public ChallengeVersionVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询题目版本列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 题目版本分页列表
     */
    @Override
    public TableDataInfo<ChallengeVersionVo> queryPageList(ChallengeVersionBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ChallengeVersion> lqw = buildQueryWrapper(bo);
        Page<ChallengeVersionVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的题目版本列表
     *
     * @param bo 查询条件
     * @return 题目版本列表
     */
    @Override
    public List<ChallengeVersionVo> queryList(ChallengeVersionBo bo) {
        LambdaQueryWrapper<ChallengeVersion> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ChallengeVersion> buildQueryWrapper(ChallengeVersionBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ChallengeVersion> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(ChallengeVersion::getId);
        lqw.eq(bo.getChallengeId() != null, ChallengeVersion::getChallengeId, bo.getChallengeId());
        lqw.like(StringUtils.isNotBlank(bo.getChallengeName()), ChallengeVersion::getChallengeName, bo.getChallengeName());
        lqw.eq(bo.getDraftId() != null, ChallengeVersion::getDraftId, bo.getDraftId());
        lqw.eq(StringUtils.isNotBlank(bo.getVersionTag()), ChallengeVersion::getVersionTag, bo.getVersionTag());
        lqw.eq(StringUtils.isNotBlank(bo.getVersionDescription()), ChallengeVersion::getVersionDescription, bo.getVersionDescription());
        return lqw;
    }

    /**
     * 新增题目版本
     * <p>
     * 发版即入库：创建版本成功后，同步维护 t_challenge.latest_version_id，
     * 使题目状态从"草稿中"变为"已入库"。
     *
     * @param bo 题目版本
     * @return 是否新增成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ChallengeVersionBo bo) {
        ChallengeVersion add = MapstructUtils.convert(bo, ChallengeVersion.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
            // 发版即入库：更新题目最新版本ID
            Challenge challengeUpdate = new Challenge();
            challengeUpdate.setId(add.getChallengeId());
            challengeUpdate.setLatestVersionId(add.getId());
            challengeMapper.updateById(challengeUpdate);
        }
        return flag;
    }

    /**
     * 修改题目版本
     *
     * @param bo 题目版本
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(ChallengeVersionBo bo) {
        ChallengeVersion update = MapstructUtils.convert(bo, ChallengeVersion.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验（发版即入库，校验题目完整性）
     */
    private void validEntityBeforeSave(ChallengeVersion entity) {
        // 版本号必填
        if (StringUtils.isBlank(entity.getVersionTag())) {
            throw new ServiceException("版本号不能为空");
        }
        // 入库前完整性校验：加载关联草稿并检查是否满足入库条件
        if (entity.getDraftId() == null) {
            throw new ServiceException("发版必须关联题目草稿");
        }
        ChallengeDraft draft = challengeDraftMapper.selectById(entity.getDraftId());
        if (draft == null) {
            throw new ServiceException("关联的题目草稿不存在，无法发版");
        }
        validatePublishReady(draft);
    }

    /**
     * 入库前完整性校验（对应 CTF 入库流程的"检查与入库"环节）
     * <p>
     * 前端步骤条/入库检查页会做即时提示，这里作为最终闸门，保证入库的题目内容完整可用。
     *
     * @param draft 待发版草稿
     */
    private void validatePublishReady(ChallengeDraft draft) {
        DraftConfig config = draft.getConfig();
        if (config == null) {
            throw new ServiceException("题目内容为空，无法入库，请先完善题目内容");
        }
        // 题干必填
        if (StringUtils.isBlank(config.getStem())) {
            throw new ServiceException("请先填写题干描述，才能入库");
        }
        // Flag 至少一个，且静态Flag内容必填
        List<DraftConfig.Flag> flags = config.getFlags();
        if (flags == null || flags.isEmpty()) {
            throw new ServiceException("请至少配置一个Flag，才能入库");
        }
        for (int i = 0; i < flags.size(); i++) {
            DraftConfig.Flag flag = flags.get(i);
            if (flag == null) {
                continue;
            }
            // 静态Flag内容必填（DraftConfig 已配置多态反序列化，静态Flag会还原为 StaticFlag 实例）
            if (flag instanceof DraftConfig.StaticFlag staticFlag) {
                if (StringUtils.isBlank(staticFlag.getContent())) {
                    throw new ServiceException(String.format("Flag %d（静态）：内容不能为空", i + 1));
                }
            }
        }
        // 容器题：至少一个靶机，且每个靶机已选镜像、端口合法
        if ("container".equals(config.getRunType())) {
            List<DraftConfig.ContainerTarget> targets = config.getContainerTargets();
            if (targets == null || targets.isEmpty()) {
                throw new ServiceException("容器题至少需要配置一个靶机，才能入库");
            }
            for (int i = 0; i < targets.size(); i++) {
                DraftConfig.ContainerTarget target = targets.get(i);
                if (target == null) {
                    continue;
                }
                if (StringUtils.isBlank(target.getName())) {
                    throw new ServiceException(String.format("靶机 %d：名称不能为空", i + 1));
                }
                if (target.getImageId() == null) {
                    throw new ServiceException(String.format("靶机 %d「%s」：请选择镜像", i + 1, target.getName()));
                }
                if (target.getPorts() != null) {
                    for (Map.Entry<String, DraftConfig.PortConfig> entry : target.getPorts().entrySet()) {
                        DraftConfig.PortConfig port = entry.getValue();
                        if (port == null) {
                            continue;
                        }
                        if (port.getInternalPort() == null || port.getInternalPort() <= 0) {
                            throw new ServiceException(String.format("靶机 %d「%s」端口「%s」：内部端口必须为正整数",
                                i + 1, target.getName(), entry.getKey()));
                        }
                    }
                }
            }
        }
    }

    /**
     * 校验并批量删除题目版本信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
