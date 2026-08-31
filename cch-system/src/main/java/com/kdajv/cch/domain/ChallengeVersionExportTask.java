package com.kdajv.cch.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kdajv.cch.domain.bo.ChallengeVersionExportTaskBo;
import com.kdajv.cch.domain.vo.ChallengeVersionExportTaskVo;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 题目版本导出任务对象 t_challenge_version_export_task
 *
 * @author system
 * @date 2026-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_challenge_version_export_task")
@AutoMappers({
    @AutoMapper(target = ChallengeVersionExportTaskBo.class),
    @AutoMapper(target = ChallengeVersionExportTaskVo.class)
})
public class ChallengeVersionExportTask extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
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
     * 重试次数
     */
    private Integer retryCount;

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
     * 是否导出容器镜像文件（true-导出镜像tar包，false-仅保留镜像地址）
     */
    private Boolean includeImages;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 删除标志
     */
    @TableLogic
    private Long delFlag;
}
