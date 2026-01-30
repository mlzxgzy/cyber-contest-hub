package com.kdajv.cch.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.kdajv.cch.domain.DraftConfig;
import com.kdajv.cch.domain.bo.ChallengeDraftBo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 题目草稿视图对象 t_challenge_draft
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ChallengeDraftBo.class)
public class ChallengeDraftVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 派生父草稿ID
     */
    @ExcelProperty(value = "派生父草稿ID")
    private Long parentId;

    /**
     * 题目ID
     */
    @ExcelProperty(value = "题目ID")
    private Long challengeId;

    /**
     * 题目名称
     */
    @ExcelProperty(value = "题目名称")
    private String challengeName;

    /**
     * 草稿描述
     */
    @ExcelProperty(value = "草稿描述")
    private String challengeDescription;

    /**
     * 配置
     */
    @ExcelProperty(value = "配置")
    private DraftConfig config;

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
