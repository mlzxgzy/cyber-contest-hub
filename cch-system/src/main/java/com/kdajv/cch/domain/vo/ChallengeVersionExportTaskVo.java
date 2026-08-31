package com.kdajv.cch.domain.vo;

import com.kdajv.cch.domain.ChallengeVersionExportTask;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题目版本导出任务视图对象 t_challenge_version_export_task
 *
 * @author system
 * @date 2026-01-30
 */
@Data
@AutoMapper(target = ChallengeVersionExportTask.class)
public class ChallengeVersionExportTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 题目版本ID（关联t_challenge_version）
     */
    private Long versionId;

    /**
     * 版本号（冗余字段，便于查询）
     */
    private String versionTag;

    /**
     * 题目名称（冗余字段，便于显示）
     */
    private String challengeName;

    /**
     * 任务状态（0-待处理，1-处理中，2-已完成，3-失败）
     */
    private Integer taskStatus;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 任务状态文本
     */
    private String taskStatusText;

    /**
     * OSS文件ID（关联sys_oss表）
     */
    private Long ossFileId;

    /**
     * OSS文件名
     */
    private String ossFileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件大小文本（格式化）
     */
    private String fileSizeText;

    /**
     * 临时下载链接（生成时填充）
     */
    private String downloadUrl;

    /**
     * 文件过期时间（完成时间+保留时间）
     */
    private LocalDateTime expireTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
