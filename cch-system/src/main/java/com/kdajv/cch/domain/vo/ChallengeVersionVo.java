package com.kdajv.cch.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.kdajv.cch.domain.ChallengeVersion;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 题目版本视图对象 t_challenge_version
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ChallengeVersion.class)
public class ChallengeVersionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

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
     * 草稿ID
     */
    @ExcelProperty(value = "草稿ID")
    private Long draftId;

    /**
     * 版本号
     */
    @ExcelProperty(value = "版本号")
    private String versionTag;

    /**
     * 版本描述
     */
    @ExcelProperty(value = "版本描述")
    private String versionDescription;


}
