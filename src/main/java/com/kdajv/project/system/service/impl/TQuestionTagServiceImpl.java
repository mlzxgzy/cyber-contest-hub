package com.kdajv.project.system.service.impl;

import com.kdajv.project.system.domain.TQuestionTag;
import com.kdajv.project.system.mapper.TQuestionTagMapper;
import com.kdajv.project.system.service.ITQuestionTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 题目TagService业务层处理
 * 
 * @author GZY
 * @date 2024-12-25
 */
@Service
public class TQuestionTagServiceImpl implements ITQuestionTagService 
{
    @Autowired
    private TQuestionTagMapper tQuestionTagMapper;

    /**
     * 查询题目Tag
     * 
     * @param id 题目Tag主键
     * @return 题目Tag
     */
    @Override
    public TQuestionTag selectTQuestionTagById(Long id)
    {
        return tQuestionTagMapper.selectTQuestionTagById(id);
    }

    /**
     * 查询题目Tag列表
     * 
     * @param tQuestionTag 题目Tag
     * @return 题目Tag
     */
    @Override
    public List<TQuestionTag> selectTQuestionTagList(TQuestionTag tQuestionTag)
    {
        return tQuestionTagMapper.selectTQuestionTagList(tQuestionTag);
    }

    /**
     * 新增题目Tag
     * 
     * @param tQuestionTag 题目Tag
     * @return 结果
     */
    @Override
    public int insertTQuestionTag(TQuestionTag tQuestionTag)
    {
        return tQuestionTagMapper.insertTQuestionTag(tQuestionTag);
    }

    /**
     * 修改题目Tag
     * 
     * @param tQuestionTag 题目Tag
     * @return 结果
     */
    @Override
    public int updateTQuestionTag(TQuestionTag tQuestionTag)
    {
        return tQuestionTagMapper.updateTQuestionTag(tQuestionTag);
    }

    /**
     * 批量删除题目Tag
     * 
     * @param ids 需要删除的题目Tag主键
     * @return 结果
     */
    @Override
    public int deleteTQuestionTagByIds(Long[] ids)
    {
        return tQuestionTagMapper.deleteTQuestionTagByIds(ids);
    }

    /**
     * 删除题目Tag信息
     * 
     * @param id 题目Tag主键
     * @return 结果
     */
    @Override
    public int deleteTQuestionTagById(Long id)
    {
        return tQuestionTagMapper.deleteTQuestionTagById(id);
    }
}
