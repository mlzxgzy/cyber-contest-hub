package com.kdajv.cch.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.kdajv.cch.domain.Challenge;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 题目列表视图对象 t_challenge
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Challenge.class)
public class ChallengeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 题目编码（唯一业务编码）
     */
    @ExcelProperty(value = "题目编码")
    private String code;

    /**
     * 题目类型
     */
    @ExcelProperty(value = "题目类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "cch_question_categroy")
    private String category;

    /**
     * 题目名称
     */
    @ExcelProperty(value = "题目名称")
    private String name;

    /**
     * 题目备注
     */
    @ExcelProperty(value = "题目备注")
    private String remark;

    /**
     * 题目最新版ID
     */
    @ExcelProperty(value = "题目最新版ID")
    private Long latestVersionId;

    /**
     * 是否已入库（latestVersionId 非空即已发版入库）
     */
    private Boolean published;

    /**
     * 题目状态（0-草稿中，1-已入库，2-已停用）
     */
    private Integer status;

    /**
     * 最新版本号（关联版本表补充）
     */
    private String latestVersionTag;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;


}
