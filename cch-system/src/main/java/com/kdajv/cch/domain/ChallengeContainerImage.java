package com.kdajv.cch.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kdajv.cch.domain.bo.ChallengeContainerImageBo;
import com.kdajv.cch.domain.vo.ChallengeContainerImageVo;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 挑战容器镜像对象 t_challenge_container_image
 *
 * @author Lingma
 * @date 2026-01-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_challenge_container_image")
@AutoMappers({
    @AutoMapper(target = ChallengeContainerImageBo.class),
    @AutoMapper(target = ChallengeContainerImageVo.class)})
public class ChallengeContainerImage extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 题目ID
     */
    private Long challengeId;

    /**
     * 镜像名称
     */
    private String imageName;

    /**
     * 镜像标签
     */
    private String imageTag;

    /**
     * 镜像大小(字节)
     */
    private Long imageSize;

    /**
     * 镜像文件存储路径
     */
    private String filePath;

    /**
     * 镜像文件SHA256哈希值
     */
    private String fileHash;

    /**
     * 上传状态(uploading:上传中,uploaded:已上传,validating:验证中,available:可用,error:错误)
     */
    private String status;

    /**
     * 上传进度(百分比)
     */
    private BigDecimal progress;

    /**
     * 错误信息
     */
    private String errorMessage;
}
