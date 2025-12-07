package com.kdajv.cch.domain.vo;

import com.kdajv.cch.domain.ChallengeFile;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 题目文件视图对象 t_challenge_file
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ChallengeFile.class)
public class ChallengeFileVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 题目id
     */
    @ExcelProperty(value = "题目id")
    private Long challengeId;

    /**
     * 文件名
     */
    @ExcelProperty(value = "文件名")
    private String fileName;

    /**
     * 原名
     */
    @ExcelProperty(value = "原名")
    private String originalName;

    /**
     * 文件后缀名
     */
    @ExcelProperty(value = "文件后缀名")
    private String fileSuffix;

    /**
     * URL地址
     */
    @ExcelProperty(value = "URL地址")
    private String url;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 服务商
     */
    @ExcelProperty(value = "服务商")
    private String service;


}
