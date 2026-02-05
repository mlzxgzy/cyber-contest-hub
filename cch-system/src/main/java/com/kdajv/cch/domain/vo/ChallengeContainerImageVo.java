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
     * 镜像名称（用于展示，通常为 name:tag 形式）
     */
    private String imageName;

    /**
     * 镜像拉取地址（用于 docker pull / service 创建的实际镜像地址）
     */
    private String pullAddress;

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
