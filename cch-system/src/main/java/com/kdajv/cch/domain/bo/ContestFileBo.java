package com.kdajv.cch.domain.bo;

import com.kdajv.cch.domain.ContestFile;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 竞赛文件业务对象 t_contest_file
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContestFileBo extends BaseEntity {

    /**
     * 主键
     */
    private Long id;

    /**
     * 项目ID（竞赛项目）
     */
    @NotNull(message = "项目ID不能为空", groups = {AddGroup.class})
    private Long projectId;

    /**
     * OSS文件ID
     */
    @NotNull(message = "OSS文件ID不能为空", groups = {AddGroup.class})
    private Long ossId;

    /**
     * 文件标签
     */
    @Size(max = 50, message = "文件标签长度不能超过50字符", groups = {AddGroup.class})
    private String fileTag;

    /**
     * 文件名（用于显示，不存储）
     */
    private String fileName;

    /**
     * 原始文件名（用于显示，不存储）
     */
    private String originalName;

    /**
     * 文件URL（用于显示，不存储）
     */
    private String url;
}
