package com.kdajv.project.system.service.impl;

import com.kdajv.common.utils.DateUtils;
import com.kdajv.project.system.domain.TQuestionAttachment;
import com.kdajv.project.system.mapper.TQuestionAttachmentMapper;
import com.kdajv.project.system.service.ITQuestionAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 题目附件Service业务层处理
 * 
 * @author GZY
 * @date 2024-12-25
 */
@Service
public class TQuestionAttachmentServiceImpl implements ITQuestionAttachmentService 
{
    @Autowired
    private TQuestionAttachmentMapper tQuestionAttachmentMapper;

    /**
     * 查询题目附件
     * 
     * @param id 题目附件主键
     * @return 题目附件
     */
    @Override
    public TQuestionAttachment selectTQuestionAttachmentById(Long id)
    {
        return tQuestionAttachmentMapper.selectTQuestionAttachmentById(id);
    }

    /**
     * 查询题目附件列表
     * 
     * @param tQuestionAttachment 题目附件
     * @return 题目附件
     */
    @Override
    public List<TQuestionAttachment> selectTQuestionAttachmentList(TQuestionAttachment tQuestionAttachment)
    {
        return tQuestionAttachmentMapper.selectTQuestionAttachmentList(tQuestionAttachment);
    }

    /**
     * 新增题目附件
     * 
     * @param tQuestionAttachment 题目附件
     * @return 结果
     */
    @Override
    public int insertTQuestionAttachment(TQuestionAttachment tQuestionAttachment)
    {
        tQuestionAttachment.setCreateTime(DateUtils.getNowDate());
        return tQuestionAttachmentMapper.insertTQuestionAttachment(tQuestionAttachment);
    }

    /**
     * 修改题目附件
     * 
     * @param tQuestionAttachment 题目附件
     * @return 结果
     */
    @Override
    public int updateTQuestionAttachment(TQuestionAttachment tQuestionAttachment)
    {
        tQuestionAttachment.setUpdateTime(DateUtils.getNowDate());
        return tQuestionAttachmentMapper.updateTQuestionAttachment(tQuestionAttachment);
    }

    /**
     * 批量删除题目附件
     * 
     * @param ids 需要删除的题目附件主键
     * @return 结果
     */
    @Override
    public int deleteTQuestionAttachmentByIds(Long[] ids)
    {
        return tQuestionAttachmentMapper.deleteTQuestionAttachmentByIds(ids);
    }

    /**
     * 删除题目附件信息
     * 
     * @param id 题目附件主键
     * @return 结果
     */
    @Override
    public int deleteTQuestionAttachmentById(Long id)
    {
        return tQuestionAttachmentMapper.deleteTQuestionAttachmentById(id);
    }
}
