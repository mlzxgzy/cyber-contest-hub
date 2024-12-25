package com.kdajv.project.system.domain;

import com.kdajv.framework.aspectj.lang.annotation.Excel;
import com.kdajv.framework.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 竞赛题目对象 t_competition_question
 * 
 * @author GZY
 * @date 2024-12-25
 */
public class TCompetitionQuestion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 竞赛ID */
    @Excel(name = "竞赛ID")
    private Long competitionId;

    /** 竞赛名称 */
    @Excel(name = "竞赛名称")
    private String competitionName;

    /** 题目ID */
    @Excel(name = "题目ID")
    private Long questionId;

    /** 题目名称 */
    @Excel(name = "题目名称")
    private String questionName;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setCompetitionId(Long competitionId) 
    {
        this.competitionId = competitionId;
    }

    public Long getCompetitionId() 
    {
        return competitionId;
    }
    public void setCompetitionName(String competitionName) 
    {
        this.competitionName = competitionName;
    }

    public String getCompetitionName() 
    {
        return competitionName;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("competitionId", getCompetitionId())
            .append("competitionName", getCompetitionName())
            .append("questionId", getQuestionId())
            .append("questionName", getQuestionName())
            .toString();
    }
}
