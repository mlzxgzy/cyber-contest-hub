package com.kdajv.project.system.service;

import com.kdajv.project.system.domain.TCompetitionCompetitor;

import java.util.List;

/**
 * 竞赛选手Service接口
 * 
 * @author GZY
 * @date 2024-12-25
 */
public interface ITCompetitionCompetitorService 
{
    /**
     * 查询竞赛选手
     * 
     * @param id 竞赛选手主键
     * @return 竞赛选手
     */
    public TCompetitionCompetitor selectTCompetitionCompetitorById(Long id);

    /**
     * 查询竞赛选手列表
     * 
     * @param tCompetitionCompetitor 竞赛选手
     * @return 竞赛选手集合
     */
    public List<TCompetitionCompetitor> selectTCompetitionCompetitorList(TCompetitionCompetitor tCompetitionCompetitor);

    /**
     * 新增竞赛选手
     * 
     * @param tCompetitionCompetitor 竞赛选手
     * @return 结果
     */
    public int insertTCompetitionCompetitor(TCompetitionCompetitor tCompetitionCompetitor);

    /**
     * 修改竞赛选手
     * 
     * @param tCompetitionCompetitor 竞赛选手
     * @return 结果
     */
    public int updateTCompetitionCompetitor(TCompetitionCompetitor tCompetitionCompetitor);

    /**
     * 批量删除竞赛选手
     * 
     * @param ids 需要删除的竞赛选手主键集合
     * @return 结果
     */
    public int deleteTCompetitionCompetitorByIds(Long[] ids);

    /**
     * 删除竞赛选手信息
     * 
     * @param id 竞赛选手主键
     * @return 结果
     */
    public int deleteTCompetitionCompetitorById(Long id);
}
