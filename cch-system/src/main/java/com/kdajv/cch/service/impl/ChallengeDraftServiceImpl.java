package com.kdajv.cch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kdajv.cch.domain.Challenge;
import com.kdajv.cch.domain.ChallengeDraft;
import com.kdajv.cch.domain.bo.ChallengeDraftBo;
import com.kdajv.cch.domain.vo.ChallengeDraftVo;
import com.kdajv.cch.mapper.ChallengeMapper;
import com.kdajv.cch.mapper.ChallengeDraftMapper;
import com.kdajv.cch.service.IChallengeDraftService;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目草稿Service业务层处理
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ChallengeDraftServiceImpl implements IChallengeDraftService {

    private final ChallengeDraftMapper baseMapper;
    private final ChallengeMapper challengeMapper;

    /**
     * 查询题目草稿
     *
     * @param id 主键
     * @return 题目草稿
     */
    @Override
    public ChallengeDraftVo queryById(Long id) {
        return getVoWithVersion(id);
    }

    /**
     * 分页查询题目草稿列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 题目草稿分页列表
     */
    @Override
    public TableDataInfo<ChallengeDraftVo> queryPageList(ChallengeDraftBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ChallengeDraft> lqw = buildQueryWrapper(bo);
        Page<ChallengeDraftVo> result = baseMapper.selectPageDraftList(pageQuery.build(), lqw);
        fillDraftVersion(result.getRecords());
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的题目草稿列表
     *
     * @param bo 查询条件
     * @return 题目草稿列表
     */
    @Override
    public List<ChallengeDraftVo> queryList(ChallengeDraftBo bo) {
        LambdaQueryWrapper<ChallengeDraft> lqw = buildQueryWrapper(bo);
        List<ChallengeDraftVo> list = baseMapper.selectDraftList(lqw);
        fillDraftVersion(list);
        return list;
    }

    /**
     * 查询最新的题目草稿
     *
     * @param challengeId 题目ID
     * @return 最新的题目草稿
     */
    @Override
    public ChallengeDraftVo queryTop1ByChallengeIdOrderByCreateTimeDesc(Long challengeId) {
        // 查询最新的草稿记录（create_time 相同以 id 兜底，与题目列表搜索的最新草稿语义保持一致）
        LambdaQueryWrapper<ChallengeDraft> draftQueryWrapper = Wrappers.lambdaQuery();
        draftQueryWrapper.eq(ChallengeDraft::getChallengeId, challengeId);
        draftQueryWrapper.orderByDesc(ChallengeDraft::getCreateTime);
        draftQueryWrapper.orderByDesc(ChallengeDraft::getId);
        draftQueryWrapper.last("LIMIT 1");
        ChallengeDraftVo vo = baseMapper.selectVoOne(draftQueryWrapper);
        if (vo != null) {
            fillDraftVersion(Collections.singletonList(vo));
        }
        return vo;
    }

    private LambdaQueryWrapper<ChallengeDraft> buildQueryWrapper(ChallengeDraftBo bo) {
        LambdaQueryWrapper<ChallengeDraft> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(ChallengeDraft::getId);
        lqw.eq(bo.getChallengeId() != null, ChallengeDraft::getChallengeId, bo.getChallengeId());
        lqw.like(StringUtils.isNotBlank(bo.getChallengeName()), ChallengeDraft::getChallengeName, bo.getChallengeName());
        lqw.eq(StringUtils.isNotBlank(bo.getChallengeDescription()), ChallengeDraft::getChallengeDescription, bo.getChallengeDescription());
        return lqw;
    }

    /**
     * 新增题目草稿
     *
     * @param bo 题目草稿
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(ChallengeDraftBo bo) {
        ChallengeDraft add = MapstructUtils.convert(bo, ChallengeDraft.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改题目草稿
     *
     * @param bo 题目草稿
     * @return 是否修改成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChallengeDraftVo updateByBo(ChallengeDraftBo bo) {
        // 同步更新题目基本信息（交由后端统一维护一致性）
        Challenge challengeUpdate = new Challenge();
        challengeUpdate.setId(bo.getChallengeId());
        challengeUpdate.setCategory(bo.getChallengeCategory());
        challengeUpdate.setName(bo.getChallengeName());
        challengeUpdate.setRemark(bo.getChallengeRemark());
        challengeMapper.updateById(challengeUpdate);

        // 根据操作类型决定是直接更新还是新增版本
        String operateType = bo.getOperateType();
        if ("edit".equals(operateType)) {
            // 编辑模式：直接更新现有版本，不新增
            ChallengeDraft update = MapstructUtils.convert(bo, ChallengeDraft.class);
            validEntityBeforeSave(update);
            baseMapper.updateById(update);
            return getVoWithVersion(bo.getId());
        } else {
            // 保存/派生模式：每次保存都新增一条草稿记录，保留历史
            ChallengeDraft update = MapstructUtils.convert(bo, ChallengeDraft.class);
            // 设置派生父草稿ID为当前草稿的ID，形成树状结构
            update.setParentId(bo.getId());
            // 确保由数据库生成新主键
            update.setId(null);
            validEntityBeforeSave(update);
            baseMapper.insert(update);
            return getVoWithVersion(update.getId());
        }
    }

    /**
     * 查询草稿并填充版本号（供保存/派生后返回给前端展示"第N版"）
     *
     * @param id 草稿ID
     * @return 草稿VO（含版本号）
     */
    private ChallengeDraftVo getVoWithVersion(Long id) {
        ChallengeDraftVo vo = baseMapper.selectVoById(id);
        if (vo != null) {
            fillDraftVersion(Collections.singletonList(vo));
        }
        return vo;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ChallengeDraft entity) {
        //TODO 做一些数据校验,如唯一约束
        
        // 校验容器靶机配置
        if (entity.getConfig() != null && "container".equals(entity.getConfig().getRunType())) {
            var containerTargets = entity.getConfig().getContainerTargets();
            if (containerTargets != null) {
                for (int i = 0; i < containerTargets.size(); i++) {
                    var target = containerTargets.get(i);
                    if (target != null) {
                        if (StringUtils.isBlank(target.getName())) {
                            throw new ServiceException(String.format("靶机 %d：名称不能为空", i + 1));
                        }
                    }
                }
            }
        }
    }

    /**
     * 校验并批量删除题目草稿信息
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
     * 查询题目草稿的版本历史列表（用于构建树状结构）
     *
     * @param challengeId 题目ID
     * @return 题目草稿版本列表（按创建时间倒序）
     */
    @Override
    public List<ChallengeDraftVo> queryHistoryListByChallengeId(Long challengeId) {
        LambdaQueryWrapper<ChallengeDraft> lqw = Wrappers.lambdaQuery();
        lqw.eq(ChallengeDraft::getChallengeId, challengeId);
        lqw.orderByDesc(ChallengeDraft::getCreateTime);
        // 排除config字段，减少数据传输量并提高安全性
        lqw.select(
            ChallengeDraft::getId,
            ChallengeDraft::getParentId,
            ChallengeDraft::getChallengeId,
            ChallengeDraft::getChallengeName,
            ChallengeDraft::getChallengeDescription,
            ChallengeDraft::getCreateTime,
            ChallengeDraft::getUpdateTime,
            ChallengeDraft::getCreateBy,
            ChallengeDraft::getUpdateBy,
            ChallengeDraft::getCreateDept
        );
        List<ChallengeDraftVo> result = baseMapper.selectVoList(lqw);
        // 确保config字段为null（即使查询时已排除，也做二次保障）
        if (result != null) {
            result.forEach(vo -> vo.setConfig(null));
            // 按创建时间升序为每个草稿计算版本号（第1版为最早创建的草稿）
            fillDraftVersion(result);
        }
        return result;
    }

    /**
     * 为草稿列表填充版本号（draftVersion 为计算字段不入库）
     * 规则：按同一题目下草稿的创建时间升序、ID升序编号，最早的草稿为第1版
     *
     * @param drafts 草稿VO列表
     */
    private void fillDraftVersion(List<ChallengeDraftVo> drafts) {
        if (drafts == null || drafts.isEmpty()) {
            return;
        }
        // 收集涉及到的题目ID
        Set<Long> challengeIds = drafts.stream()
            .map(ChallengeDraftVo::getChallengeId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (challengeIds.isEmpty()) {
            return;
        }
        // 轻量查询：仅拉取编号所需的字段
        LambdaQueryWrapper<ChallengeDraft> lqw = Wrappers.lambdaQuery();
        lqw.in(ChallengeDraft::getChallengeId, challengeIds);
        lqw.select(ChallengeDraft::getId, ChallengeDraft::getChallengeId, ChallengeDraft::getCreateTime);
        List<ChallengeDraft> allDrafts = baseMapper.selectList(lqw);
        if (allDrafts == null || allDrafts.isEmpty()) {
            return;
        }
        // 按题目分组后组内升序排序并编号
        Map<Long, Map<Long, Integer>> versionMap = new HashMap<>();
        allDrafts.stream()
            .collect(Collectors.groupingBy(ChallengeDraft::getChallengeId))
            .forEach((challengeId, list) -> {
                list.sort(Comparator
                    .comparing(ChallengeDraft::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(ChallengeDraft::getId));
                Map<Long, Integer> idToVersion = new HashMap<>(list.size());
                for (int i = 0; i < list.size(); i++) {
                    idToVersion.put(list.get(i).getId(), i + 1);
                }
                versionMap.put(challengeId, idToVersion);
            });
        // 回填版本号
        drafts.forEach(vo -> {
            Map<Long, Integer> idToVersion = versionMap.get(vo.getChallengeId());
            if (idToVersion != null) {
                vo.setDraftVersion(idToVersion.get(vo.getId()));
            }
        });
    }

    /**
     * 从指定版本派生新版本
     *
     * @param parentDraftId 父版本草稿ID
     * @return 派生后的新草稿数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChallengeDraftVo forkFromDraftId(Long parentDraftId) {
        // 查询父版本草稿
        ChallengeDraftVo parentDraft = baseMapper.selectVoById(parentDraftId);
        if (parentDraft == null) {
            throw new ServiceException("父版本草稿不存在");
        }

        // 创建新的草稿，基于父版本
        ChallengeDraftBo newDraftBo = MapstructUtils.convert(parentDraft, ChallengeDraftBo.class);
        // 设置父ID
        newDraftBo.setParentId(parentDraftId);
        // 清空ID，让数据库生成新ID
        newDraftBo.setId(null);

        ChallengeDraft newDraft = MapstructUtils.convert(newDraftBo, ChallengeDraft.class);
        validEntityBeforeSave(newDraft);
        baseMapper.insert(newDraft);

        return getVoWithVersion(newDraft.getId());
    }
}
