package com.kdajv.cch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kdajv.cch.domain.ChallengeVersion;
import com.kdajv.cch.domain.bo.ChallengeVersionBo;
import com.kdajv.cch.domain.vo.ChallengeVersionVo;
import com.kdajv.cch.mapper.ChallengeVersionMapper;
import com.kdajv.cch.service.IChallengeVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

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
     *
     * @param bo 题目版本
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(ChallengeVersionBo bo) {
        ChallengeVersion add = MapstructUtils.convert(bo, ChallengeVersion.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
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
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ChallengeVersion entity) {
        //TODO 做一些数据校验,如唯一约束
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
