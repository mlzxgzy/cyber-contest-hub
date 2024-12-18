package com.kdajv.project.system.service;

import java.util.List;
import com.kdajv.project.system.domain.TCompetitor;

/**
 * 选手Service接口
 * 
 * @author GZY
 * @date 2024-12-18
 */
public interface ITCompetitorService 
{
    /**
     * 查询选手
     * 
     * @param id 选手主键
     * @return 选手
     */
    public TCompetitor selectTCompetitorById(Long id);

    /**
     * 查询选手列表
     * 
     * @param tCompetitor 选手
     * @return 选手集合
     */
    public List<TCompetitor> selectTCompetitorList(TCompetitor tCompetitor);

    /**
     * 新增选手
     * 
     * @param tCompetitor 选手
     * @return 结果
     */
    public int insertTCompetitor(TCompetitor tCompetitor);

    /**
     * 修改选手
     * 
     * @param tCompetitor 选手
     * @return 结果
     */
    public int updateTCompetitor(TCompetitor tCompetitor);

    /**
     * 批量删除选手
     * 
     * @param ids 需要删除的选手主键集合
     * @return 结果
     */
    public int deleteTCompetitorByIds(Long[] ids);

    /**
     * 删除选手信息
     * 
     * @param id 选手主键
     * @return 结果
     */
    public int deleteTCompetitorById(Long id);
}
