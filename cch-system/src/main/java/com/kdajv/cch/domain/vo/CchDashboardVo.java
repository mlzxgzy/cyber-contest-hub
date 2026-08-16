package com.kdajv.cch.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 首页仪表盘统计数据
 *
 * @author system
 * @date 2026-03-21
 */
@Data
public class CchDashboardVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 核心数据总览
     */
    private Overview overview;

    /**
     * 题目类型分布
     */
    private List<NameValue> categoryDistribution;

    /**
     * 项目类型分布
     */
    private List<NameValue> projectTypeDistribution;

    /**
     * 容器镜像状态分布
     */
    private List<NameValue> imageStatusDistribution;

    /**
     * 导出任务状态分布
     */
    private List<NameValue> exportTaskStatusDistribution;

    /**
     * 近6个月创建趋势
     */
    private List<TrendItem> trend;

    /**
     * 最近创建的项目
     */
    private List<ProjectVo> recentProjects;

    @Data
    public static class Overview implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Long projectCount;
        private Long challengeCount;
        private Long versionCount;
        private Long draftCount;
        private Long fileCount;
        private Long imageCount;
        private Long mockTestCount;
        private Long exportTaskCount;
        private Long projectChallengeCount;
        private Long projectMemberCount;
        private Long contestFileCount;
    }

    @Data
    public static class NameValue implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String name;
        private Long value;
    }

    @Data
    public static class TrendItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String month;
        private Long challengeCount;
        private Long versionCount;
        private Long projectCount;
    }
}
