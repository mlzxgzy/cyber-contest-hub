package com.kdajv.project.system.service;

import com.kdajv.project.system.domain.TQuestionAttachment;

import java.util.List;

/**
 * 题目附件Service接口
 * 
 * @author GZY
 * @date 2024-12-25
 */
public interface ITQuestionAttachmentService 
{
    /**
     * 查询题目附件
     * 
     * @param id 题目附件主键
     * @return 题目附件
     */
    public TQuestionAttachment selectTQuestionAttachmentById(Long id);

    /**
     * 查询题目附件列表
     * 
     * @param tQuestionAttachment 题目附件
     * @return 题目附件集合
     */
    public List<TQuestionAttachment> selectTQuestionAttachmentList(TQuestionAttachment tQuestionAttachment);

    /**
     * 新增题目附件
     * 
     * @param tQuestionAttachment 题目附件
     * @return 结果
     */
    public int insertTQuestionAttachment(TQuestionAttachment tQuestionAttachment);

    /**
     * 修改题目附件
     * 
     * @param tQuestionAttachment 题目附件
     * @return 结果
     */
    public int updateTQuestionAttachment(TQuestionAttachment tQuestionAttachment);

    /**
     * 批量删除题目附件
     * 
     * @param ids 需要删除的题目附件主键集合
     * @return 结果
     */
    public int deleteTQuestionAttachmentByIds(Long[] ids);

    /**
     * 删除题目附件信息
     * 
     * @param id 题目附件主键
     * @return 结果
     */
    public int deleteTQuestionAttachmentById(Long id);
}
