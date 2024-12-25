package com.kdajv.project.system.domain;

import com.kdajv.framework.aspectj.lang.annotation.Excel;
import com.kdajv.framework.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 题目附件对象 t_question_attachment
 * 
 * @author GZY
 * @date 2024-12-25
 */
public class TQuestionAttachment extends BaseEntity
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

    /** 文件名 */
    @Excel(name = "文件名")
    private String name;

    /** 文件备注 */
    @Excel(name = "文件备注")
    private String description;

    /** 文件下载地址 */
    @Excel(name = "文件下载地址")
    private String path;

    /** 文件类型 */
    @Excel(name = "文件类型")
    private String type;

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
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }
    public void setPath(String path) 
    {
        this.path = path;
    }

    public String getPath() 
    {
        return path;
    }
    public void setType(String type) 
    {
        this.type = type;
    }

    public String getType() 
    {
        return type;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("questionId", getQuestionId())
            .append("questionName", getQuestionName())
            .append("name", getName())
            .append("description", getDescription())
            .append("path", getPath())
            .append("type", getType())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
