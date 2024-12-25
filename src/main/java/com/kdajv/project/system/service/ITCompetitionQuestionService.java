package com.kdajv.project.system.service;

import com.kdajv.project.system.domain.TCompetitionQuestion;

import java.util.List;

/**
 * 竞赛题目Service接口
 * 
 * @author GZY
 * @date 2024-12-25
 */
public interface ITCompetitionQuestionService 
{
    /**
     * 查询竞赛题目
     * 
     * @param id 竞赛题目主键
     * @return 竞赛题目
     */
    public TCompetitionQuestion selectTCompetitionQuestionById(Long id);

    /**
     * 查询竞赛题目列表
     * 
     * @param tCompetitionQuestion 竞赛题目
     * @return 竞赛题目集合
     */
    public List<TCompetitionQuestion> selectTCompetitionQuestionList(TCompetitionQuestion tCompetitionQuestion);

    /**
     * 新增竞赛题目
     * 
     * @param tCompetitionQuestion 竞赛题目
     * @return 结果
     */
    public int insertTCompetitionQuestion(TCompetitionQuestion tCompetitionQuestion);

    /**
     * 修改竞赛题目
     * 
     * @param tCompetitionQuestion 竞赛题目
     * @return 结果
     */
    public int updateTCompetitionQuestion(TCompetitionQuestion tCompetitionQuestion);

    /**
     * 批量删除竞赛题目
     * 
     * @param ids 需要删除的竞赛题目主键集合
     * @return 结果
     */
    public int deleteTCompetitionQuestionByIds(Long[] ids);

    /**
     * 删除竞赛题目信息
     * 
     * @param id 竞赛题目主键
     * @return 结果
     */
    public int deleteTCompetitionQuestionById(Long id);
}
