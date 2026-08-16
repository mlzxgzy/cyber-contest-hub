package com.kdajv.cch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kdajv.cch.domain.Challenge;
import com.kdajv.cch.domain.ChallengeDraft;
import com.kdajv.cch.domain.ChallengeVersion;
import com.kdajv.cch.domain.DraftConfig;
import com.kdajv.cch.domain.bo.ChallengeBo;
import com.kdajv.cch.domain.bo.ChallengeDraftBo;
import com.kdajv.cch.domain.vo.ChallengeDraftVo;
import com.kdajv.cch.domain.vo.ChallengeVo;
import com.kdajv.cch.mapper.ChallengeDraftMapper;
import com.kdajv.cch.mapper.ChallengeMapper;
import com.kdajv.cch.mapper.ChallengeVersionMapper;
import com.kdajv.cch.service.IChallengeService;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目列表Service业务层处理
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ChallengeServiceImpl implements IChallengeService {

    private final ChallengeMapper baseMapper;
    private final ChallengeDraftMapper challengeDraftMapper;
    private final ChallengeVersionMapper challengeVersionMapper;

    /**
     * 查询题目列表
     *
     * @param id 主键
     * @return 题目列表
     */
    @Override
    public ChallengeVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询题目列表列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 题目列表分页列表
     */
    @Override
    public TableDataInfo<ChallengeVo> queryPageList(ChallengeBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Challenge> lqw = buildQueryWrapper(bo);
        Page<ChallengeVo> result = baseMapper.selectPageChallengeList(pageQuery.build(), lqw);
        fillPublishInfo(result.getRecords());
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的题目列表列表
     *
     * @param bo 查询条件
     * @return 题目列表列表
     */
    @Override
    public List<ChallengeVo> queryList(ChallengeBo bo) {
        LambdaQueryWrapper<Challenge> lqw = buildQueryWrapper(bo);
        List<ChallengeVo> list = baseMapper.selectChallengeList(lqw);
        fillPublishInfo(list);
        return list;
    }

    private LambdaQueryWrapper<Challenge> buildQueryWrapper(ChallengeBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Challenge> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Challenge::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getCategory()), Challenge::getCategory, bo.getCategory());
        lqw.like(StringUtils.isNotBlank(bo.getName()), Challenge::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getRemark()), Challenge::getRemark, bo.getRemark());
        // 入库状态筛选：true=已入库（latestVersionId 非空），false=草稿中（latestVersionId 为空）
        if (bo.getPublished() != null) {
            lqw.isNotNull(bo.getPublished(), Challenge::getLatestVersionId);
            lqw.isNull(!bo.getPublished(), Challenge::getLatestVersionId);
        }
        return lqw;
    }

    /**
     * 为列表补充入库状态与最新版本号（latest_version_id 未在 t_challenge 中冗余存储版本号，需关联版本表）
     *
     * @param list 查询结果
     */
    private void fillPublishInfo(List<ChallengeVo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> versionIds = list.stream()
            .map(ChallengeVo::getLatestVersionId)
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toSet());
        if (versionIds.isEmpty()) {
            list.forEach(vo -> vo.setPublished(false));
            return;
        }
        Map<Long, String> versionTagMap = challengeVersionMapper.selectBatchIds(versionIds).stream()
            .collect(Collectors.toMap(ChallengeVersion::getId, ChallengeVersion::getVersionTag, (a, b) -> a));
        for (ChallengeVo vo : list) {
            boolean published = vo.getLatestVersionId() != null;
            vo.setPublished(published);
            if (published) {
                vo.setLatestVersionTag(versionTagMap.get(vo.getLatestVersionId()));
            }
        }
    }

    /**
     * 新增题目列表
     *
     * @param bo 题目列表
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(ChallengeBo bo) {
        Challenge add = MapstructUtils.convert(bo, Challenge.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改题目列表
     *
     * @param bo 题目列表
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(ChallengeBo bo) {
        Challenge update = MapstructUtils.convert(bo, Challenge.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Challenge entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除题目列表信息
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

    /**
     * 初始化一道新题目（含首个草稿），用于"新增题目入库"一体化流程
     *
     * @param bo 题目基础信息 + 初始草稿配置
     * @return 初始化的草稿信息（含新题目ID）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChallengeDraftVo initChallengeWithDraft(ChallengeDraftBo bo) {
        // 基础校验：名称与分类必填（草稿BO上的分组校验不适用于初始化场景，这里手动校验）
        if (StringUtils.isBlank(bo.getChallengeName())) {
            throw new ServiceException("题目名称不能为空");
        }
        if (StringUtils.isBlank(bo.getChallengeCategory())) {
            throw new ServiceException("题目类型不能为空");
        }
        // 名称唯一性校验（未软删除的同名题目）
        Long duplicateCount = baseMapper.selectCount(Wrappers.<Challenge>lambdaQuery()
            .eq(Challenge::getName, bo.getChallengeName().trim()));
        if (duplicateCount != null && duplicateCount > 0) {
            throw new ServiceException("已存在同名题目：" + bo.getChallengeName().trim());
        }

        // 1. 创建题目骨架（t_challenge）
        Challenge challenge = new Challenge();
        challenge.setCategory(bo.getChallengeCategory());
        challenge.setName(bo.getChallengeName().trim());
        challenge.setRemark(StringUtils.isBlank(bo.getChallengeRemark()) ? "" : bo.getChallengeRemark());
        baseMapper.insert(challenge);

        // 2. 创建首个草稿（t_challenge_draft），config 为空时给默认对象，避免后续编辑页空指针
        ChallengeDraft draft = new ChallengeDraft();
        draft.setChallengeId(challenge.getId());
        draft.setChallengeName(challenge.getName());
        draft.setChallengeDescription(bo.getChallengeDescription());
        draft.setConfig(bo.getConfig() == null ? new DraftConfig() : bo.getConfig());
        challengeDraftMapper.insert(draft);

        log.info("[initChallengeWithDraft] 新题目初始化完成 challengeId={}, draftId={}", challenge.getId(), draft.getId());
        return challengeDraftMapper.selectVoById(draft.getId());
    }
}
