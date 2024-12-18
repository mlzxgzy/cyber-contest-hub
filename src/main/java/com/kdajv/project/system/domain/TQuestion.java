package com.kdajv.project.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.kdajv.framework.aspectj.lang.annotation.Excel;
import com.kdajv.framework.web.domain.BaseEntity;

/**
 * 题目对象 t_question
 * 
 * @author GZY
 * @date 2024-12-18
 */
public class TQuestion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 名称 */
    @Excel(name = "名称")
    private String name;

    /** 描述 */
    @Excel(name = "描述")
    private String description;

    /** 类型 */
    @Excel(name = "类型")
    private String category;

    /** 难度 */
    @Excel(name = "难度")
    private Long difficult;

    /** Flag */
    @Excel(name = "Flag")
    private String flag;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
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
    public void setCategory(String category) 
    {
        this.category = category;
    }

    public String getCategory() 
    {
        return category;
    }
    public void setDifficult(Long difficult) 
    {
        this.difficult = difficult;
    }

    public Long getDifficult() 
    {
        return difficult;
    }
    public void setFlag(String flag) 
    {
        this.flag = flag;
    }

    public String getFlag() 
    {
        return flag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("description", getDescription())
            .append("category", getCategory())
            .append("difficult", getDifficult())
            .append("flag", getFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
