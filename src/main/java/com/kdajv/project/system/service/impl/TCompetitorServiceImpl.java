package com.kdajv.project.system.service.impl;

import java.util.List;
import com.kdajv.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kdajv.project.system.mapper.TCompetitorMapper;
import com.kdajv.project.system.domain.TCompetitor;
import com.kdajv.project.system.service.ITCompetitorService;

/**
 * 选手Service业务层处理
 * 
 * @author GZY
 * @date 2024-12-18
 */
@Service
public class TCompetitorServiceImpl implements ITCompetitorService 
{
    @Autowired
    private TCompetitorMapper tCompetitorMapper;

    /**
     * 查询选手
     * 
     * @param id 选手主键
     * @return 选手
     */
    @Override
    public TCompetitor selectTCompetitorById(Long id)
    {
        return tCompetitorMapper.selectTCompetitorById(id);
    }

    /**
     * 查询选手列表
     * 
     * @param tCompetitor 选手
     * @return 选手
     */
    @Override
    public List<TCompetitor> selectTCompetitorList(TCompetitor tCompetitor)
    {
        return tCompetitorMapper.selectTCompetitorList(tCompetitor);
    }

    /**
     * 新增选手
     * 
     * @param tCompetitor 选手
     * @return 结果
     */
    @Override
    public int insertTCompetitor(TCompetitor tCompetitor)
    {
        tCompetitor.setCreateTime(DateUtils.getNowDate());
        return tCompetitorMapper.insertTCompetitor(tCompetitor);
    }

    /**
     * 修改选手
     * 
     * @param tCompetitor 选手
     * @return 结果
     */
    @Override
    public int updateTCompetitor(TCompetitor tCompetitor)
    {
        tCompetitor.setUpdateTime(DateUtils.getNowDate());
        return tCompetitorMapper.updateTCompetitor(tCompetitor);
    }

    /**
     * 批量删除选手
     * 
     * @param ids 需要删除的选手主键
     * @return 结果
     */
    @Override
    public int deleteTCompetitorByIds(Long[] ids)
    {
        return tCompetitorMapper.deleteTCompetitorByIds(ids);
    }

    /**
     * 删除选手信息
     * 
     * @param id 选手主键
     * @return 结果
     */
    @Override
    public int deleteTCompetitorById(Long id)
    {
        return tCompetitorMapper.deleteTCompetitorById(id);
    }
}
