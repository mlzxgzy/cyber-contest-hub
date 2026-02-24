package com.kdajv.cch.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kdajv.cch.domain.bo.ContestFileBo;
import com.kdajv.cch.domain.vo.ContestFileVo;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 竞赛文件对象 t_contest_file
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_contest_file")
@AutoMappers({
    @AutoMapper(target = ContestFileVo.class),
    @AutoMapper(target = ContestFileBo.class),
})
public class ContestFile extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 项目ID（竞赛项目）
     */
    private Long projectId;

    /**
     * OSS文件ID
     */
    private Long ossId;

    /**
     * 文件标签
     */
    private String fileTag;

    /**
     * 删除标志
     */
    @TableLogic
    private Long delFlag;
}
