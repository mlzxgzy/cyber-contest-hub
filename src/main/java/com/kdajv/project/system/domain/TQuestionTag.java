package com.kdajv.project.system.domain;

import com.kdajv.framework.aspectj.lang.annotation.Excel;
import com.kdajv.framework.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 题目Tag对象 t_question_tag
 * 
 * @author GZY
 * @date 2024-12-25
 */
public class TQuestionTag extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 题目ID */
    @Excel(name = "题目ID")
    private Long questionId;

    /** 题目名称 */
    @Excel(name = "题目名称")
    private String questionName;

    /** Tag */
    @Excel(name = "Tag")
    private String tag;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setQuestionId(Long questionId) 
    {
        this.questionId = questionId;
    }

    public Long getQuestionId() 
    {
        return questionId;
    }
    public void setQuestionName(String questionName) 
    {
        this.questionName = questionName;
    }

    public String getQuestionName() 
    {
        return questionName;
    }
    public void setTag(String tag) 
    {
        this.tag = tag;
    }

    public String getTag() 
    {
        return tag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("questionId", getQuestionId())
            .append("questionName", getQuestionName())
            .append("tag", getTag())
            .toString();
    }
}
