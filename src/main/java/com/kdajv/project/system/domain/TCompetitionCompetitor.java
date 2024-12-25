package com.kdajv.project.system.domain;

import com.kdajv.framework.aspectj.lang.annotation.Excel;
import com.kdajv.framework.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 竞赛选手对象 t_competition_competitor
 * 
 * @author GZY
 * @date 2024-12-25
 */
public class TCompetitionCompetitor extends BaseEntity
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

    /** 选手ID */
    @Excel(name = "选手ID")
    private Long competitorId;

    /** 选手姓名 */
    @Excel(name = "选手姓名")
    private String competitorName;

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
    public void setCompetitorId(Long competitorId) 
    {
        this.competitorId = competitorId;
    }

    public Long getCompetitorId() 
    {
        return competitorId;
    }
    public void setCompetitorName(String competitorName) 
    {
        this.competitorName = competitorName;
    }

    public String getCompetitorName() 
    {
        return competitorName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("competitionId", getCompetitionId())
            .append("competitionName", getCompetitionName())
            .append("competitorId", getCompetitorId())
            .append("competitorName", getCompetitorName())
            .toString();
    }
}
