package com.kdajv.cch.domain.bo;

import com.kdajv.cch.domain.Challenge;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 题目列表业务对象 t_challenge
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Challenge.class, reverseConvertGenerate = false)
public class ChallengeBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 题目类型
     */
    @NotBlank(message = "题目类型不能为空", groups = {AddGroup.class, EditGroup.class})
    private String category;

    /**
     * 题目名称
     */
    @NotBlank(message = "题目名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String name;

    /**
     * 题目备注
     */
    @NotBlank(message = "题目备注不能为空", groups = {AddGroup.class, EditGroup.class})
    private String remark;

    /**
     * 入库状态筛选（查询用）：true=已入库（有最新版本），false=草稿中（未发版），null=全部
     */
    private Boolean published;

    /**
     * 难度筛选（查询用，对应最新草稿 config.difficulty 的字典值，如 3medium）
     */
    private String difficulty;

    /**
     * 知识点筛选（查询用，对应最新草稿 config.knowledge 中的标签）
     */
    private String knowledge;


}
