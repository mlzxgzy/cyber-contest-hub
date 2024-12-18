package com.kdajv.project.system.service;

import java.util.List;
import com.kdajv.project.system.domain.TQuestion;

/**
 * 题目Service接口
 * 
 * @author GZY
 * @date 2024-12-18
 */
public interface ITQuestionService 
{
    /**
     * 查询题目
     * 
     * @param id 题目主键
     * @return 题目
     */
    public TQuestion selectTQuestionById(Long id);

    /**
     * 查询题目列表
     * 
     * @param tQuestion 题目
     * @return 题目集合
     */
    public List<TQuestion> selectTQuestionList(TQuestion tQuestion);

    /**
     * 新增题目
     * 
     * @param tQuestion 题目
     * @return 结果
     */
    public int insertTQuestion(TQuestion tQuestion);

    /**
     * 修改题目
     * 
     * @param tQuestion 题目
     * @return 结果
     */
    public int updateTQuestion(TQuestion tQuestion);

    /**
     * 批量删除题目
     * 
     * @param ids 需要删除的题目主键集合
     * @return 结果
     */
    public int deleteTQuestionByIds(Long[] ids);

    /**
     * 删除题目信息
     * 
     * @param id 题目主键
     * @return 结果
     */
    public int deleteTQuestionById(Long id);
}
