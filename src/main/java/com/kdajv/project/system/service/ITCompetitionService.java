package com.kdajv.project.system.service;

import java.util.List;
import com.kdajv.project.system.domain.TCompetition;

/**
 * 竞赛Service接口
 * 
 * @author GZY
 * @date 2024-12-18
 */
public interface ITCompetitionService 
{
    /**
     * 查询竞赛
     * 
     * @param id 竞赛主键
     * @return 竞赛
     */
    public TCompetition selectTCompetitionById(Long id);

    /**
     * 查询竞赛列表
     * 
     * @param tCompetition 竞赛
     * @return 竞赛集合
     */
    public List<TCompetition> selectTCompetitionList(TCompetition tCompetition);

    /**
     * 新增竞赛
     * 
     * @param tCompetition 竞赛
     * @return 结果
     */
    public int insertTCompetition(TCompetition tCompetition);

    /**
     * 修改竞赛
     * 
     * @param tCompetition 竞赛
     * @return 结果
     */
    public int updateTCompetition(TCompetition tCompetition);

    /**
     * 批量删除竞赛
     * 
     * @param ids 需要删除的竞赛主键集合
     * @return 结果
     */
    public int deleteTCompetitionByIds(Long[] ids);

    /**
     * 删除竞赛信息
     * 
     * @param id 竞赛主键
     * @return 结果
     */
    public int deleteTCompetitionById(Long id);
}
