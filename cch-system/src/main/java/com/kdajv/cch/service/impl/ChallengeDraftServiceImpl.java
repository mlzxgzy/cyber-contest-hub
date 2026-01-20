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
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

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
        return baseMapper.selectVoById(id);
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
        Page<ChallengeDraftVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
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
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 查询最新的题目草稿
     *
     * @param challengeId 题目ID
     * @return 最新的题目草稿
     */
    @Override
    public ChallengeDraftVo queryTop1ByChallengeIdOrderByCreateTimeDesc(Long challengeId) {
        // 查询最新的草稿记录
        LambdaQueryWrapper<ChallengeDraft> draftQueryWrapper = Wrappers.lambdaQuery();
        draftQueryWrapper.eq(ChallengeDraft::getChallengeId, challengeId);
        draftQueryWrapper.orderByDesc(ChallengeDraft::getCreateTime);
        draftQueryWrapper.last("LIMIT 1");
        return baseMapper.selectVoOne(draftQueryWrapper);
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

        // 每次保存都新增一条草稿记录，保留历史
        ChallengeDraft update = MapstructUtils.convert(bo, ChallengeDraft.class);
        // 设置派生父草稿ID为当前草稿的ID，形成树状结构
        update.setParentId(bo.getId());
        // 确保由数据库生成新主键
        update.setId(null);
        validEntityBeforeSave(update);
        baseMapper.insert(update);
        return baseMapper.selectVoById(update.getId());
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ChallengeDraft entity) {
        //TODO 做一些数据校验,如唯一约束
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
}
