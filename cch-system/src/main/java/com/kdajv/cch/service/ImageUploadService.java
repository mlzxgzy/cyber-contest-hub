package com.kdajv.cch.service;

import cn.hutool.core.util.IdUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.LoadImageCmd;
import com.kdajv.cch.domain.ChallengeContainerImage;
import com.kdajv.cch.domain.vo.ChallengeContainerImageVo;
import com.kdajv.cch.domain.bo.ChallengeContainerImageBo;
import com.kdajv.cch.mapper.ChallengeContainerImageMapper;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.oss.core.OssClient;
import org.dromara.common.oss.entity.UploadResult;
import org.dromara.common.oss.factory.OssFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 镜像上传服务
 *
 * @author Lingma
 * @date 2026-01-27
 */
@Slf4j
@Service
public class ImageUploadService {

    @Autowired
    private ChallengeContainerImageMapper challengeContainerImageMapper;

    @Autowired
    private ICchContainerConfigService containerConfigService;

    // 上传进度跟踪
    private final Map<Long, Double> uploadProgress = new ConcurrentHashMap<>();

    /**
     * 上传镜像文件
     *
     * @param challengeId 题目ID
     * @param imageName   镜像名称
     * @param file        上传的文件
     * @return 镜像记录信息
     */
    public ChallengeContainerImageVo uploadImage(Long challengeId, String imageName, MultipartFile file) {
        // 创建镜像记录 - 上传阶段
        ChallengeContainerImageBo imageBo = new ChallengeContainerImageBo();
        imageBo.setChallengeId(challengeId);
        imageBo.setImageName(imageName);
        imageBo.setImageTag("latest");
        imageBo.setImageSize(file.getSize());
        imageBo.setStatus("uploading");
        imageBo.setProgress(java.math.BigDecimal.valueOf(0.0));

        // 生成初始文件路径（暂时的标识）
        String uniqueFileName = IdUtil.fastSimpleUUID() + "_" + file.getOriginalFilename();
        imageBo.setFilePath(uniqueFileName);

        // 插入数据库记录
        insertByBo(imageBo);
        Long imageId = imageBo.getId();

        // 启动异步上传任务到OSS
        startAsyncOssUpload(imageId, file, imageName);

        // 返回镜像记录
        return queryById(imageId);
    }

    /**
     * 查询挑战容器镜像
     */
    public ChallengeContainerImageVo queryById(Long id) {
        return challengeContainerImageMapper.selectVoById(id);
    }

    /**
     * 新增挑战容器镜像
     */
    public Boolean insertByBo(ChallengeContainerImageBo bo) {
        ChallengeContainerImage add = new com.kdajv.cch.domain.ChallengeContainerImage();
        add = MapstructUtils.convert(bo, add);
        // 设置默认值
        if (bo.getProgress() == null) {
            bo.setProgress(java.math.BigDecimal.ZERO);
        }
        if (bo.getImageTag() == null) {
            bo.setImageTag("latest");
        }
        if (bo.getStatus() == null) {
            bo.setStatus("uploading");
        }
        validEntityBeforeSave(add);
        boolean flag = challengeContainerImageMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;

    }

    /**
     * 修改挑战容器镜像
     */
    public Boolean updateByBo(ChallengeContainerImageBo bo) {
        ChallengeContainerImage update = new ChallengeContainerImage();
        org.springframework.beans.BeanUtils.copyProperties(bo, update);
        validEntityBeforeSave(update);
        return challengeContainerImageMapper.updateById(update) > 0;
    }

    /**
     * 修改挑战容器镜像状态
     */
    public Boolean updateStatus(Long id, String status, Double progress, String errorMessage) {
        ChallengeContainerImage update = new ChallengeContainerImage();
        update.setId(id);
        update.setStatus(status);
        if (progress != null) {
            update.setProgress(java.math.BigDecimal.valueOf(progress));
        }
        update.setErrorMessage(errorMessage);
        return challengeContainerImageMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(com.kdajv.cch.domain.ChallengeContainerImage entity) {
        // 做一些数据校验
    }

    /**
     * 启动异步OSS上传任务
     */
    void startAsyncOssUpload(Long imageId, MultipartFile file, String imageName) {
        try {
            log.info("开始上传镜像文件到OSS: {} 大小: {}", file.getOriginalFilename(), file.getSize());

            // 使用OSS服务上传文件
            OssClient storage = OssFactory.instance();
            String fileExtension = getFileExtension(file.getOriginalFilename());

            UploadResult uploadResult = storage.uploadSuffix(file.getInputStream(), fileExtension, file.getSize(), file.getContentType());

            // 上传成功后更新数据库记录
            ChallengeContainerImageBo updateBo = new ChallengeContainerImageBo();
            updateBo.setId(imageId);
            updateBo.setFilePath(uploadResult.getFilename()); // 设置OSS返回的URL
            updateBo.setStatus("uploaded"); // 设置为已上传状态
            updateBo.setProgress(BigDecimal.valueOf(100.0)); // 设置进度为100%
            updateBo.setErrorMessage(null);

            log.info("镜像文件上传到OSS成功: {} -> {}", file.getOriginalFilename(), uploadResult.getUrl());

            // 上传完成，状态保持为uploaded，等待手动Load
            updateByBo(updateBo);

        } catch (IOException e) {
            log.error("镜像上传到OSS失败: ", e);
            // 更新数据库记录为错误状态
            updateStatus(imageId, "error", 0.0, e.getMessage());
        } catch (Exception e) {
            log.error("镜像上传过程中发生未知错误: ", e);
            // 更新数据库记录为错误状态
            updateStatus(imageId, "error", 0.0, e.getMessage());
        } finally {
            // 清除进度记录
            uploadProgress.remove(imageId);
        }
    }

    /**
     * 开始Load处理阶段
     */
    void startLoadProcess(Long imageId, String ossKey) {
        try {
            log.info("开始Load处理镜像: ID={}", imageId);

            // 更新状态为验证中
            updateStatus(imageId, "validating", 50.0, null);

            // 获取活跃的Docker客户端
            Object activeClient = containerConfigService.getActiveClient();
            if (!(activeClient instanceof DockerClient dockerClient)) {
                log.error("没有活跃的Docker连接，无法Load镜像: ID={}", imageId);
                updateStatus(imageId, "error", 100.0, "没有活跃的Docker连接");
                return;
            }

            InputStream inputStream = downloadImageFromOSSKey(ossKey);
            if (inputStream == null) {
                log.error("无法获取镜像文件流: ID={}", imageId);
                updateStatus(imageId, "error", 100.0, "无法获取镜像文件流");
                return;
            }
            // 执行Load操作
            LoadImageCmd loadImageCmd = dockerClient.loadImageCmd(inputStream);
            log.info("开始加载镜像到Docker...");
            loadImageCmd.exec();
            log.info("镜像Load操作完成");
            // Load完成，更新为可用状态
            updateStatus(imageId, "available", 100.0, null);
            log.info("镜像Load处理完成: ID={}", imageId);
        } catch (Exception e) {
            log.error("镜像Load处理失败: ", e);
            updateStatus(imageId, "error", 100.0, e.getMessage());
        }
    }

    /**
     * 从OSS获取镜像文件流
     */
    private InputStream downloadImageFromOSSKey(String key) {
        try {
            // 使用OSS服务下载文件流
            OssClient ossClient = OssFactory.instance();

            // 创建管道输入输出流，实现流式传输
            java.io.PipedInputStream pipedInputStream = new java.io.PipedInputStream();
            java.io.PipedOutputStream pipedOutputStream = new java.io.PipedOutputStream(pipedInputStream);

            // 在单独线程中执行下载，将数据写入PipedOutputStream
            Thread downloadThread = new Thread(() -> {
                try {
                    ossClient.download(key, pipedOutputStream, length -> {
                    });
                } catch (Exception e) {
                    log.error("从OSS下载文件失败: {}", key, e);
                } finally {
                    try {
                        pipedOutputStream.close();
                    } catch (IOException ioException) {
                        log.error("关闭输出流失败: ", ioException);
                    }
                }
            });

            // 启动下载线程
            downloadThread.start();

            return pipedInputStream;
        } catch (Exception e) {
            log.error("获取OSS文件流失败: {}", key, e);
            return null;
        }
    }

    /**
     * 获取上传进度
     */
    public Double getUploadProgress(Long imageId) {
        Double progress = uploadProgress.get(imageId);
        if (progress == null) {
            // 如果不在内存中，则从数据库获取最新状态
            ChallengeContainerImageVo image = queryById(imageId);
            if (image != null && image.getProgress() != null) {
                return image.getProgress().doubleValue();
            }
            return 0.0;
        }
        return progress;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "tar"; // 默认扩展名
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    /**
     * 手动Load镜像到Docker
     */
    public Boolean manualLoadImage(Long imageId) {
        ChallengeContainerImageVo image = queryById(imageId);
        if (image == null) {
            log.error("镜像记录不存在: ID={}", imageId);
            return false;
        }

        // 检查镜像是否已经加载过或处于其他非上传完成状态
        if (!"uploaded".equals(image.getStatus())) {
            log.error("镜像状态不允许Load: ID={}, 当前状态={}", imageId, image.getStatus());
            return false;
        }

        // 开始Load处理
        startLoadProcess(imageId, image.getFilePath());
        return true;
    }
}
