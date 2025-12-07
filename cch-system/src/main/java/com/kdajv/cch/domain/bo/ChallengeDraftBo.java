package com.kdajv.cch.domain.bo;

import com.kdajv.cch.domain.ChallengeDraft;
import com.kdajv.cch.domain.DraftConfig;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 题目草稿业务对象 t_challenge_draft
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ChallengeDraft.class, reverseConvertGenerate = false)
public class ChallengeDraftBo extends BaseEntity {

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
     * 草稿描述
     */
    private String challengeDescription;

    /**
     * 配置
     */
    private DraftConfig config;


}
