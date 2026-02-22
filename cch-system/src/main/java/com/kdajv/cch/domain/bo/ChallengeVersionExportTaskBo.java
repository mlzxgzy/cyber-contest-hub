package com.kdajv.cch.domain.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * 题目版本导出任务业务对象 t_challenge_version_export_task
 *
 * @author system
 * @date 2026-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChallengeVersionExportTaskBo extends BaseEntity {

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
     * 任务状态（0-待处理，1-处理中，2-已完成，3-失败）
     */
    private Integer taskStatus;

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
}
