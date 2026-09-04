package com.kdajv.cch.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.kdajv.cch.container.ContainerClient;
import com.kdajv.cch.domain.ChallengeContainerImage;
import com.kdajv.cch.domain.vo.CchContainerConfigVo;
import com.kdajv.cch.domain.vo.ChallengeContainerImageVo;
import com.kdajv.cch.domain.bo.ChallengeContainerImageBo;
import com.kdajv.cch.mapper.ChallengeContainerImageMapper;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.oss.core.OssClient;
import org.dromara.common.oss.entity.UploadResult;
import org.dromara.common.oss.factory.OssFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
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
     * 重新上传镜像文件（用于上传/Load失败的镜像重传，复用原镜像记录）
     *
     * @param imageId 镜像记录ID
     * @param file    新的镜像文件
     * @return 镜像记录信息
     */
    public ChallengeContainerImageVo reuploadImage(Long imageId, MultipartFile file) {
        ChallengeContainerImageVo image = queryById(imageId);
        if (image == null) {
            log.error("重新上传镜像失败，镜像记录不存在: ID={}", imageId);
            throw new IllegalArgumentException("镜像记录不存在: ID=" + imageId);
        }

        // 重置镜像记录状态，等待重新上传
        String imageName = StringUtils.isNotBlank(image.getImageName())
            ? image.getImageName()
            : (file.getOriginalFilename() != null ? file.getOriginalFilename().split("\\.")[0] : "default-image");

        LambdaUpdateWrapper<ChallengeContainerImage> wrapper = new LambdaUpdateWrapper<ChallengeContainerImage>()
            .eq(ChallengeContainerImage::getId, imageId)
            .set(ChallengeContainerImage::getStatus, "uploading")
            .set(ChallengeContainerImage::getProgress, BigDecimal.ZERO)
            .set(ChallengeContainerImage::getImageSize, file.getSize())
            .set(ChallengeContainerImage::getImageName, imageName)
            .set(ChallengeContainerImage::getFilePath, null)
            .set(ChallengeContainerImage::getPullAddress, null)
            .set(ChallengeContainerImage::getErrorMessage, null);
        challengeContainerImageMapper.update(null, wrapper);

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
            // 上传完成，状态保持为uploaded，等待手动Load
            updateByBo(updateBo);

            log.info("镜像文件上传到OSS成功: {} -> {}", file.getOriginalFilename(), uploadResult.getUrl());
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
    void startLoadProcess(Long imageId, Long challengeId, String ossKey) {
        try {
            log.info("开始Load处理镜像: ID={}", imageId);

            // 更新状态为验证中
            updateStatus(imageId, "validating", 50.0, null);

            // 获取活跃的容器客户端（当前实现为 Docker，后续可扩展为 K8s）
            ContainerClient containerClient = containerConfigService.getActiveClient();
            if (containerClient == null) {
                log.error("没有活跃的容器连接，无法Load镜像: ID={}", imageId);
                updateStatus(imageId, "error", 100.0, "没有活跃的容器连接");
                return;
            }
            InputStream inputStream = downloadImageFromOSSKey(ossKey);
            if (inputStream == null) {
                log.error("无法获取镜像文件流: ID={}", imageId);
                updateStatus(imageId, "error", 100.0, "无法获取镜像文件流");
                return;
            }
            // 执行Load操作（通过容器客户端抽象）
            String result = containerClient.loadImage(inputStream);
            // Load完成，更新为可用状态
            ChallengeContainerImageBo updateBo = new ChallengeContainerImageBo();
            updateBo.setId(imageId);
            String imageWithTag = result.trim().replaceFirst("Loaded image: ", "").trim();
            String[] imageWithTagArray = imageWithTag.split(":");
            String imageName = imageWithTagArray[0];
            String tag = "latest";
            if (imageWithTagArray.length == 2) {
                tag = imageWithTagArray[1];
            } else {
                log.error("分割镜像名称时出现问题，msg:{}，分割数据：{}", result, result);
            }

            // Registry信息现在直接从“活跃Docker配置”里读取（与Docker配置一起保存）
            CchContainerConfigVo activeDockerConfig = containerConfigService.getActiveInstance();

            String registryUrl = activeDockerConfig != null ? activeDockerConfig.getRegistryUrl() : null;
            String repo = activeDockerConfig != null ? activeDockerConfig.getRegistryRepo() : null;

            // Docker镜像名不应包含协议头（如 http:// 或 https://）
            String cleanRegistryUrl = StringUtils.isNotBlank(registryUrl) ? registryUrl.replaceFirst("^https?://", "") : null;

            if (StringUtils.isNotBlank(cleanRegistryUrl) && StringUtils.isBlank(repo)) {
                repo = "images";
            }

            // 构建目标镜像地址
            final String finalImageName;
            if (StringUtils.isNotBlank(cleanRegistryUrl)) {
                finalImageName = "%s/%s/%d/%s".formatted(cleanRegistryUrl, repo, challengeId, imageName);
                log.info("Registry已配置，使用Registry地址 tag镜像: {}", finalImageName);
            } else {
                finalImageName = "cch/%d/%s".formatted(challengeId, imageName);
                log.info("Registry未配置，使用本地地址 tag镜像: {}", finalImageName);
            }

            containerClient.tagImage(imageWithTag, finalImageName, tag);
            containerClient.removeImage(imageWithTag);

            // 如果Registry已配置，推送镜像到Registry
            if (StringUtils.isNotBlank(cleanRegistryUrl)) {
                log.info("推送镜像到Registry: {}", finalImageName);
                containerClient.pushImage(finalImageName, tag);
                log.info("镜像推送完成: {}", finalImageName);
            }

            // 展示名称使用 Load 后得到的镜像全名（包含标签）
            updateBo.setImageName(imageWithTag);
            // 拉取地址使用实际推送到 Registry（或本地）后的完整地址（包含标签）
            updateBo.setPullAddress("%s:%s".formatted(finalImageName, tag));
            updateBo.setStatus("available"); // 设置为已上传状态
            updateBo.setProgress(BigDecimal.valueOf(100.0)); // 设置进度为100%
            updateBo.setErrorMessage(null);
            // 上传完成，状态保持为uploaded，等待手动Load
            updateByBo(updateBo);
            log.info("镜像Load操作完成：id={}，msg：{}", imageId, result);
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
            PipedInputStream pipedInputStream = new PipedInputStream();
            PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);

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
        startLoadProcess(imageId, image.getChallengeId(), image.getFilePath());
        return true;
    }
}
