package com.kdajv.project.system.service.impl;

import java.util.List;
import com.kdajv.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kdajv.project.system.mapper.TCompetitionMapper;
import com.kdajv.project.system.domain.TCompetition;
import com.kdajv.project.system.service.ITCompetitionService;

/**
 * 竞赛Service业务层处理
 * 
 * @author GZY
 * @date 2024-12-18
 */
@Service
public class TCompetitionServiceImpl implements ITCompetitionService 
{
    @Autowired
    private TCompetitionMapper tCompetitionMapper;

    /**
     * 查询竞赛
     * 
     * @param id 竞赛主键
     * @return 竞赛
     */
    @Override
    public TCompetition selectTCompetitionById(Long id)
    {
        return tCompetitionMapper.selectTCompetitionById(id);
    }

    /**
     * 查询竞赛列表
     * 
     * @param tCompetition 竞赛
     * @return 竞赛
     */
    @Override
    public List<TCompetition> selectTCompetitionList(TCompetition tCompetition)
    {
        return tCompetitionMapper.selectTCompetitionList(tCompetition);
    }

    /**
     * 新增竞赛
     * 
     * @param tCompetition 竞赛
     * @return 结果
     */
    @Override
    public int insertTCompetition(TCompetition tCompetition)
    {
        tCompetition.setCreateTime(DateUtils.getNowDate());
        return tCompetitionMapper.insertTCompetition(tCompetition);
    }

    /**
     * 修改竞赛
     * 
     * @param tCompetition 竞赛
     * @return 结果
     */
    @Override
    public int updateTCompetition(TCompetition tCompetition)
    {
        tCompetition.setUpdateTime(DateUtils.getNowDate());
        return tCompetitionMapper.updateTCompetition(tCompetition);
    }

    /**
     * 批量删除竞赛
     * 
     * @param ids 需要删除的竞赛主键
     * @return 结果
     */
    @Override
    public int deleteTCompetitionByIds(Long[] ids)
    {
        return tCompetitionMapper.deleteTCompetitionByIds(ids);
    }

    /**
     * 删除竞赛信息
     * 
     * @param id 竞赛主键
     * @return 结果
     */
    @Override
    public int deleteTCompetitionById(Long id)
    {
        return tCompetitionMapper.deleteTCompetitionById(id);
    }
}
