package com.kdajv.cch.domain.bo;

import com.kdajv.cch.domain.ChallengeFile;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 题目文件业务对象 t_challenge_file
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ChallengeFile.class, reverseConvertGenerate = false)
public class ChallengeFileBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 题目id
     */
    @NotNull(message = "题目id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long challengeId;

    /**
     * 文件名
     */
    @NotBlank(message = "文件名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fileName;

    /**
     * 原名
     */
    @NotBlank(message = "原名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String originalName;

    /**
     * 文件后缀名
     */
    @NotBlank(message = "文件后缀名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fileSuffix;

    /**
     * URL地址
     */
    @NotBlank(message = "URL地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String url;

    /**
     * 扩展字段
     */
    private String ext1;

    /**
     * 服务商
     */
    @NotBlank(message = "服务商不能为空", groups = { AddGroup.class, EditGroup.class })
    private String service;


}
