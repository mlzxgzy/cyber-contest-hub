package com.kdajv.cch.domain.bo;

import com.kdajv.cch.domain.ChallengeDraft;
import com.kdajv.cch.domain.DraftConfig;
import com.kdajv.cch.domain.vo.ChallengeDraftVo;
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
@AutoMapper(target = ChallengeDraftVo.class)
public class ChallengeDraftBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 派生父草稿ID
     */
    private Long parentId;

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
     * 题目类型（基本信息，同步到 Challenge）
     */
    @NotBlank(message = "题目类型不能为空", groups = {AddGroup.class, EditGroup.class})
    private String challengeCategory;

    /**
     * 题目备注（基本信息，同步到 Challenge）
     */
    @NotBlank(message = "题目备注不能为空", groups = {AddGroup.class, EditGroup.class})
    private String challengeRemark;

    /**
     * 草稿描述
     */
    private String challengeDescription;

    /**
     * 配置
     */
    private DraftConfig config;

    /**
     * 操作类型：edit-直接更新（不新增版本），save-保存时新增版本
     * 默认为 save
     */
    private String operateType;


}
