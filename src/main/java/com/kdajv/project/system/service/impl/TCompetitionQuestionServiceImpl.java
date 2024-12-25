package com.kdajv.project.system.service.impl;

import com.kdajv.project.system.domain.TCompetitionQuestion;
import com.kdajv.project.system.mapper.TCompetitionQuestionMapper;
import com.kdajv.project.system.service.ITCompetitionQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 竞赛题目Service业务层处理
 * 
 * @author GZY
 * @date 2024-12-25
 */
@Service
public class TCompetitionQuestionServiceImpl implements ITCompetitionQuestionService 
{
    @Autowired
    private TCompetitionQuestionMapper tCompetitionQuestionMapper;

    /**
     * 查询竞赛题目
     * 
     * @param id 竞赛题目主键
     * @return 竞赛题目
     */
    @Override
    public TCompetitionQuestion selectTCompetitionQuestionById(Long id)
    {
        return tCompetitionQuestionMapper.selectTCompetitionQuestionById(id);
    }

    /**
     * 查询竞赛题目列表
     * 
     * @param tCompetitionQuestion 竞赛题目
     * @return 竞赛题目
     */
    @Override
    public List<TCompetitionQuestion> selectTCompetitionQuestionList(TCompetitionQuestion tCompetitionQuestion)
    {
        return tCompetitionQuestionMapper.selectTCompetitionQuestionList(tCompetitionQuestion);
    }

    /**
     * 新增竞赛题目
     * 
     * @param tCompetitionQuestion 竞赛题目
     * @return 结果
     */
    @Override
    public int insertTCompetitionQuestion(TCompetitionQuestion tCompetitionQuestion)
    {
        return tCompetitionQuestionMapper.insertTCompetitionQuestion(tCompetitionQuestion);
    }

    /**
     * 修改竞赛题目
     * 
     * @param tCompetitionQuestion 竞赛题目
     * @return 结果
     */
    @Override
    public int updateTCompetitionQuestion(TCompetitionQuestion tCompetitionQuestion)
    {
        return tCompetitionQuestionMapper.updateTCompetitionQuestion(tCompetitionQuestion);
    }

    /**
     * 批量删除竞赛题目
     * 
     * @param ids 需要删除的竞赛题目主键
     * @return 结果
     */
    @Override
    public int deleteTCompetitionQuestionByIds(Long[] ids)
    {
        return tCompetitionQuestionMapper.deleteTCompetitionQuestionByIds(ids);
    }

    /**
     * 删除竞赛题目信息
     * 
     * @param id 竞赛题目主键
     * @return 结果
     */
    @Override
    public int deleteTCompetitionQuestionById(Long id)
    {
        return tCompetitionQuestionMapper.deleteTCompetitionQuestionById(id);
    }
}
