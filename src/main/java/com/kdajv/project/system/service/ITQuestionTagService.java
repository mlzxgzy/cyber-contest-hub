package com.kdajv.project.system.service;

import com.kdajv.project.system.domain.TQuestionTag;

import java.util.List;

/**
 * 题目TagService接口
 * 
 * @author GZY
 * @date 2024-12-25
 */
public interface ITQuestionTagService 
{
    /**
     * 查询题目Tag
     * 
     * @param id 题目Tag主键
     * @return 题目Tag
     */
    public TQuestionTag selectTQuestionTagById(Long id);

    /**
     * 查询题目Tag列表
     * 
     * @param tQuestionTag 题目Tag
     * @return 题目Tag集合
     */
    public List<TQuestionTag> selectTQuestionTagList(TQuestionTag tQuestionTag);

    /**
     * 新增题目Tag
     * 
     * @param tQuestionTag 题目Tag
     * @return 结果
     */
    public int insertTQuestionTag(TQuestionTag tQuestionTag);

    /**
     * 修改题目Tag
     * 
     * @param tQuestionTag 题目Tag
     * @return 结果
     */
    public int updateTQuestionTag(TQuestionTag tQuestionTag);

    /**
     * 批量删除题目Tag
     * 
     * @param ids 需要删除的题目Tag主键集合
     * @return 结果
     */
    public int deleteTQuestionTagByIds(Long[] ids);

    /**
     * 删除题目Tag信息
     * 
     * @param id 题目Tag主键
     * @return 结果
     */
    public int deleteTQuestionTagById(Long id);
}
