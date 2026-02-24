package com.kdajv.cch.domain.vo;

import com.kdajv.cch.domain.ContestFile;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 竞赛文件视图对象 t_contest_file
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
public class ContestFileVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 项目ID（竞赛项目）
     */
    private Long projectId;

    /**
     * OSS文件ID
     */
    private Long ossId;

    /**
     * 文件标签
     */
    private String fileTag;

    /**
     * 文件名（用于显示）
     */
    private String fileName;

    /**
     * 原始文件名（用于显示）
     */
    private String originalName;

    /**
     * 文件URL（用于显示）
     */
    private String url;

    /**
     * 创建时间
     */
    private Date createTime;
}
