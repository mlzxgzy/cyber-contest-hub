package com.kdajv.cch.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.kdajv.cch.domain.ContestMeta;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 项目视图对象 t_project
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
@ExcelIgnoreUnannotated
public class ProjectVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 项目类型（'normal'普通项目, 'contest'竞赛项目）
     */
    @ExcelProperty(value = "项目类型")
    private String projectType;

    /**
     * 项目名称
     */
    @ExcelProperty(value = "项目名称")
    private String name;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 竞赛meta信息（仅竞赛项目使用）
     */
    private ContestMeta meta;

    /**
     * 成员列表
     */
    private List<ProjectMemberVo> members;

    /**
     * 题目列表
     */
    private List<ProjectChallengeVo> challenges;

    /**
     * 竞赛文件列表（仅竞赛项目）
     */
    private List<ContestFileVo> contestFiles;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;
}
