package com.kdajv.project.system.service.impl;

import com.kdajv.project.system.domain.TCompetitionCompetitor;
import com.kdajv.project.system.mapper.TCompetitionCompetitorMapper;
import com.kdajv.project.system.service.ITCompetitionCompetitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 竞赛选手Service业务层处理
 * 
 * @author GZY
 * @date 2024-12-25
 */
@Service
public class TCompetitionCompetitorServiceImpl implements ITCompetitionCompetitorService 
{
    @Autowired
    private TCompetitionCompetitorMapper tCompetitionCompetitorMapper;

    /**
     * 查询竞赛选手
     * 
     * @param id 竞赛选手主键
     * @return 竞赛选手
     */
    @Override
    public TCompetitionCompetitor selectTCompetitionCompetitorById(Long id)
    {
        return tCompetitionCompetitorMapper.selectTCompetitionCompetitorById(id);
    }

    /**
     * 查询竞赛选手列表
     * 
     * @param tCompetitionCompetitor 竞赛选手
     * @return 竞赛选手
     */
    @Override
    public List<TCompetitionCompetitor> selectTCompetitionCompetitorList(TCompetitionCompetitor tCompetitionCompetitor)
    {
        return tCompetitionCompetitorMapper.selectTCompetitionCompetitorList(tCompetitionCompetitor);
    }

    /**
     * 新增竞赛选手
     * 
     * @param tCompetitionCompetitor 竞赛选手
     * @return 结果
     */
    @Override
    public int insertTCompetitionCompetitor(TCompetitionCompetitor tCompetitionCompetitor)
    {
        return tCompetitionCompetitorMapper.insertTCompetitionCompetitor(tCompetitionCompetitor);
    }

    /**
     * 修改竞赛选手
     * 
     * @param tCompetitionCompetitor 竞赛选手
     * @return 结果
     */
    @Override
    public int updateTCompetitionCompetitor(TCompetitionCompetitor tCompetitionCompetitor)
    {
        return tCompetitionCompetitorMapper.updateTCompetitionCompetitor(tCompetitionCompetitor);
    }

    /**
     * 批量删除竞赛选手
     * 
     * @param ids 需要删除的竞赛选手主键
     * @return 结果
     */
    @Override
    public int deleteTCompetitionCompetitorByIds(Long[] ids)
    {
        return tCompetitionCompetitorMapper.deleteTCompetitionCompetitorByIds(ids);
    }

    /**
     * 删除竞赛选手信息
     * 
     * @param id 竞赛选手主键
     * @return 结果
     */
    @Override
    public int deleteTCompetitionCompetitorById(Long id)
    {
        return tCompetitionCompetitorMapper.deleteTCompetitionCompetitorById(id);
    }
}
