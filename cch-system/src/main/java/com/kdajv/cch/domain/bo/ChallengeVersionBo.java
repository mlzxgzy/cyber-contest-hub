package com.kdajv.cch.domain.bo;

import com.kdajv.cch.domain.ChallengeVersion;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 题目版本业务对象 t_challenge_version
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ChallengeVersion.class, reverseConvertGenerate = false)
public class ChallengeVersionBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 题目ID
     */
    @NotNull(message = "题目ID不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long challengeId;

    /**
     * 题目名称
     */
    @NotBlank(message = "题目名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String challengeName;

    /**
     * 草稿ID
     */
    @NotNull(message = "草稿ID不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long draftId;

    /**
     * 版本号
     */
    @NotBlank(message = "版本号不能为空", groups = {AddGroup.class, EditGroup.class})
    private String versionTag;

    /**
     * 版本描述
     */
    private String versionDescription;


}
