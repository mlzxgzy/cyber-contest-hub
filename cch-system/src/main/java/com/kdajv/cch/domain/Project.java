package com.kdajv.cch.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.kdajv.cch.domain.bo.ProjectBo;
import com.kdajv.cch.domain.vo.ProjectVo;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 项目对象 t_project
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_project", autoResultMap = true)
@AutoMappers({
    @AutoMapper(target = ProjectVo.class),
    @AutoMapper(target = ProjectBo.class),
})
public class Project extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 项目类型（'normal'普通项目, 'contest'竞赛项目）
     */
    private String projectType;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 备注
     */
    private String remark;

    /**
     * 项目负责人（手填人名）
     */
    private String leader;

    /**
     * JSON字段，存储竞赛项目的额外信息（竞赛时间段、相关文件等）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ContestMeta meta;

    /**
     * 删除标志
     */
    @TableLogic
    private Long delFlag;
}
