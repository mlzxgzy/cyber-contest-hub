package com.kdajv.cch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kdajv.cch.domain.Challenge;
import com.kdajv.cch.domain.bo.ChallengeBo;
import com.kdajv.cch.domain.vo.ChallengeVo;
import com.kdajv.cch.mapper.ChallengeMapper;
import com.kdajv.cch.service.IChallengeService;
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
        Page<ChallengeVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
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
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Challenge> buildQueryWrapper(ChallengeBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Challenge> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Challenge::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getCategory()), Challenge::getCategory, bo.getCategory());
        lqw.like(StringUtils.isNotBlank(bo.getName()), Challenge::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getRemark()), Challenge::getRemark, bo.getRemark());
        return lqw;
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
}
