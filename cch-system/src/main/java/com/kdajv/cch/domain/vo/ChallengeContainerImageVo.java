package com.kdajv.cch.domain.vo;

import com.kdajv.cch.domain.ChallengeContainerImage;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 挑战容器镜像视图对象 t_challenge_container_image
 *
 * @author Lingma
 * @date 2026-01-27
 */
@Data
public class ChallengeContainerImageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
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

    /**
     * 创建时间
     */
    private Date createTime;

}
