package com.kdajv.cch.domain.bo;

import com.kdajv.cch.domain.ChallengeContainerImage;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 挑战容器镜像业务对象 t_challenge_container_image
 *
 * @author Lingma
 * @date 2026-01-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChallengeContainerImageBo extends BaseEntity {

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
     * 镜像名称
     */
    @NotBlank(message = "镜像名称不能为空", groups = {AddGroup.class, EditGroup.class})
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
    @NotBlank(message = "镜像文件存储路径不能为空", groups = {AddGroup.class, EditGroup.class})
    private String filePath;

    /**
     * 镜像文件SHA256哈希值
     */
    private String fileHash;

    /**
     * 上传状态(uploading:上传中,uploaded:已上传,validating:验证中,available:可用,error:错误)
     */
    @NotBlank(message = "上传状态不能为空", groups = {AddGroup.class, EditGroup.class})
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
