package com.kdajv.cch.service.impl;

import cn.hutool.core.date.DateTime;
import com.kdajv.cch.container.ContainerClient;
import com.kdajv.cch.domain.ChallengeVersionExportTask;
import com.kdajv.cch.domain.DraftConfig;
import com.kdajv.cch.domain.vo.ChallengeContainerImageVo;
import com.kdajv.cch.domain.vo.ChallengeFileVo;
import com.kdajv.cch.domain.vo.ChallengeVo;
import com.kdajv.cch.domain.vo.ChallengeDraftVo;
import com.kdajv.cch.domain.vo.ChallengeVersionVo;
import com.kdajv.cch.mapper.ChallengeVersionExportTaskMapper;
import com.kdajv.cch.service.IChallengeContainerImageService;
import com.kdajv.cch.service.IChallengeFileService;
import com.kdajv.cch.service.IChallengeDraftService;
import com.kdajv.cch.service.ICchContainerConfigService;
import com.kdajv.cch.service.IChallengeService;
import com.kdajv.cch.service.IChallengeVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.oss.core.OssClient;
import org.dromara.common.oss.entity.UploadResult;
import org.dromara.common.oss.enums.AccessPolicyType;
import org.dromara.common.oss.factory.OssFactory;
import org.dromara.system.domain.SysOss;
import org.dromara.system.domain.SysOssExt;
import org.dromara.system.mapper.SysOssMapper;
import org.dromara.system.service.ISysConfigService;
import org.dromara.system.service.ISysOssService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 题目版本导出任务执行器
 * 负责异步执行导出任务，将题目信息、附件、WP文件及容器镜像打包为ZIP上传到OSS
 *
 * @author system
 * @date 2026-01-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeVersionExportExecutor {

    private final ChallengeVersionExportTaskMapper taskMapper;
    private final IChallengeVersionService challengeVersionService;
    private final ISysConfigService sysConfigService;
    private final SysOssMapper sysOssMapper;
    private final IChallengeFileService challengeFileService;
    private final IChallengeDraftService challengeDraftService;
    private final IChallengeContainerImageService challengeContainerImageService;
    private final ICchContainerConfigService cchContainerConfigService;
    private final IChallengeService challengeService;
    private final ISysOssService sysOssService;

    private static final String CONFIG_KEY_FILE_RETENTION_HOURS = "cch.export.fileRetentionHours";
    private static final int DEFAULT_RETENTION_HOURS = 72;

    /**
     * 镜像地址合法格式：允许 host[:port]/[namespace/]name[:tag][@digest]
     * 只允许字母、数字、点、连字符、下划线、斜杠、冒号、@
     */
    private static final Pattern PULL_ADDRESS_PATTERN = Pattern.compile("^[a-zA-Z0-9._\\-/:@]+$");

    /**
     * 镜像地址最大长度，防止超长字符串
     */
    private static final int MAX_PULL_ADDRESS_LENGTH = 2048;

    /**
     * 错误信息最大长度，防止过长错误信息导致的问题
     */
    private static final int MAX_ERROR_MESSAGE_LENGTH = 500;

    /**
     * 缓冲区大小（8KB）
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * 异步执行导出任务
     * <p>注意：此方法标注了 {@code @Async}，Spring 事务代理在异步线程中无法正常传播，
     * 因此不在此处使用 {@code @Transactional}。各数据库操作（insert/update）均为独立操作，
     * 失败时通过 catch 块手动补偿（清理 OSS 文件、更新任务状态为失败）。</p>
     *
     * @param taskId 任务ID
     */
    @Async
    public void executeExportTask(Long taskId) {
        ChallengeVersionExportTask task = taskMapper.selectById(taskId);
        if (task == null) {
            log.error("导出任务不存在，任务ID: {}", taskId);
            return;
        }

        // 检查任务状态，只有处理中状态才能执行（队列管理器已将状态从0更新为1）
        if (task.getTaskStatus() != 1) {
            log.warn("任务状态不正确，无法执行，任务ID: {}, 当前状态: {}", taskId, task.getTaskStatus());
            return;
        }

        // 记录本次上传的OSS文件，用于失败时清理
        Long uploadedOssFileId = null;
        String uploadedOssFileName = null;

        // 落盘临时文件路径，用于兜底清理（OssClient.upload 成功后会自动删除，失败时在 finally 中补删）
        Path tmpZipPath = null;

        try {
            log.info("开始执行导出任务，任务ID: {}, 版本ID: {}", taskId, task.getVersionId());

            // 查询版本信息
            ChallengeVersionVo version = challengeVersionService.queryById(task.getVersionId());
            if (version == null) {
                throw new ServiceException("题目版本不存在");
            }

            // 查询草稿信息（含 DraftConfig）
            ChallengeDraftVo draft = null;
            if (version.getDraftId() != null) {
                draft = challengeDraftService.queryById(version.getDraftId());
            }

            // 构建ZIP包（落盘到临时文件，避免大镜像撑爆内存）
            boolean includeImages = Boolean.TRUE.equals(task.getIncludeImages());
            tmpZipPath = buildZipPackage(version, draft, includeImages);
            long fileSize = Files.size(tmpZipPath);
            log.info("ZIP包构建完成，临时文件: {}, 大小: {} bytes", tmpZipPath, fileSize);

            // 流式上传到OSS（upload(Path) 内部使用 transferManager.uploadFile，上传完成后自动删除临时文件）
            OssClient ossClient = OssFactory.instance();
            String ossKey = ossClient.getPath(null, ".zip");
            UploadResult uploadResult = ossClient.upload(tmpZipPath, ossKey, null, "application/zip");
            // 临时文件已由 OssClient 自动删除，置空引用避免 finally 重复删除
            tmpZipPath = null;

            // 保存OSS文件记录到数据库，获取OSS文件ID
            // 对 challengeName 做 sanitize，防止含特殊字符的题目名污染文件名
            String safeChallengeName = sanitizeFilename(version.getChallengeName());
            String safeVersionTag = sanitizeFilename(version.getVersionTag());
            String originalFileName = "qexport_%s_%s.zip".formatted(safeChallengeName, safeVersionTag);
            SysOssExt ext1 = new SysOssExt();
            ext1.setFileSize(fileSize);
            ext1.setContentType("application/zip");
            ext1.setBizType("challenge_version_export");
            ext1.setIsTemp(true); // 标记为临时文件，会在过期后清理

            SysOss oss = new SysOss();
            oss.setUrl(uploadResult.getUrl());
            oss.setFileSuffix(".zip");
            oss.setFileName(uploadResult.getFilename());
            oss.setOriginalName(originalFileName);
            oss.setService(ossClient.getConfigKey());
            oss.setExt1(JsonUtils.toJsonString(ext1));
            sysOssMapper.insert(oss);

            Long ossFileId = oss.getOssId();
            // 记录已上传的OSS文件，供失败时清理
            uploadedOssFileId = ossFileId;
            uploadedOssFileName = uploadResult.getFilename();

            // 获取文件保留时间（小时）
            int retentionHours = getFileRetentionHours();

            // 生成临时下载链接
            String downloadUrl = uploadResult.getUrl();
            if (AccessPolicyType.PRIVATE == ossClient.getAccessPolicy()) {
                // 如果是私有桶，生成临时URL，有效期设置为保留时间
                downloadUrl = ossClient.getPrivateUrl(uploadResult.getFilename(), Duration.ofHours(retentionHours));
            }

            // 计算过期时间
            LocalDateTime expireTime = LocalDateTime.now().plusHours(retentionHours);

            // 更新任务状态
            task.setTaskStatus(2); // 已完成
            task.setOssFileId(ossFileId);
            task.setOssFileName(uploadResult.getFilename());
            task.setFileSize(fileSize);
            task.setDownloadUrl(downloadUrl);
            task.setExpireTime(expireTime);
            task.setUpdateTime(DateTime.now());
            taskMapper.updateById(task);

            log.info("导出任务执行成功，任务ID: {}, 文件大小: {} bytes, OSS文件ID: {}",
                taskId, fileSize, ossFileId);

        } catch (Exception e) {
            log.error("导出任务执行失败，任务ID: {}", taskId, e);

            // 失败时清理已上传的OSS缓存文件，避免产生垃圾文件
            cleanupOssFile(uploadedOssFileId, uploadedOssFileName);

            // 更新任务状态为失败
            task.setTaskStatus(3); // 失败
            // 限制错误信息长度，防止过长错误信息导致的问题
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.length() > MAX_ERROR_MESSAGE_LENGTH) {
                errorMessage = errorMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH) + "...";
            }
            task.setErrorMessage(errorMessage != null ? errorMessage : "导出任务执行失败");
            task.setUpdateTime(DateTime.now());
            taskMapper.updateById(task);
        } finally {
            // 兜底：若 OssClient.upload 抛异常前未能自动删除临时文件，在此补删
            if (tmpZipPath != null) {
                try {
                    Files.deleteIfExists(tmpZipPath);
                    log.debug("已清理临时ZIP文件: {}", tmpZipPath);
                } catch (IOException ignored) {
                    log.warn("清理临时ZIP文件失败: {}", tmpZipPath);
                }
            }
        }
    }

    /**
     * 构建ZIP包，包含题目信息JSON、附件、WP文件及容器镜像
     * <p>直接落盘到系统临时目录，避免大镜像撑爆内存。
     * 调用方负责在使用完毕后删除返回的临时文件（OssClient.upload 会自动删除；
     * 若上传前抛出异常，executeExportTask 的 finally 块会兜底清理）。</p>
     *
     * @param version       版本信息
     * @param draft         草稿信息（含DraftConfig）
     * @param includeImages 是否导出容器镜像文件
     * @return 临时ZIP文件路径
     */
    private Path buildZipPackage(ChallengeVersionVo version, ChallengeDraftVo draft, boolean includeImages) throws Exception {
        String zipRootDir = "challenge_export_" + version.getId() + "/";

        Path tmpFile = Files.createTempFile("cch_export_" + version.getId() + "_", ".zip");
        log.debug("创建临时ZIP文件: {}", tmpFile);

        try (OutputStream fos = Files.newOutputStream(tmpFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            // Step 1: 写入 challenge_info.json
            writeJsonInfo(zos, zipRootDir, version, draft);

            // Step 2: 下载附件和WP文件
            // 附件先落盘临时文件再写入ZIP（附件可能较大，避免长时间占用OSS连接）
            // WP直接流式写入（WP一般较小，无需落盘）
            if (draft != null && draft.getConfig() != null) {
                DraftConfig config = draft.getConfig();
                downloadAttachmentsViaTempFile(zos, zipRootDir + "attachments/", config.getAttachments());
                downloadAttachments(zos, zipRootDir + "writeups/", config.getWriteups());
            }

            // Step 3: 导出容器镜像（仅当 includeImages=true 时才拉取镜像文件）
            if (version.getChallengeId() != null && includeImages) {
                exportContainerImages(zos, zipRootDir + "images/", version.getChallengeId());
            } else if (version.getChallengeId() != null) {
                log.info("不导出镜像文件，镜像地址已记录在 challenge_info.json 中");
            }

            zos.finish();
        } catch (Exception e) {
            // 构建失败时立即清理临时文件，不留垃圾
            try {
                Files.deleteIfExists(tmpFile);
            } catch (IOException ignored) {
            }
            throw e;
        }

        return tmpFile;
    }

    /**
     * 将题目信息序列化为JSON并写入ZIP
     */
    private void writeJsonInfo(ZipOutputStream zos, String zipRootDir,
                               ChallengeVersionVo version, ChallengeDraftVo draft) throws IOException {
        Map<String, Object> infoMap = new HashMap<>();

        // 查询并写入题目基本信息
        if (version.getChallengeId() != null) {
            try {
                ChallengeVo challenge = challengeService.queryById(version.getChallengeId());
                infoMap.put("challenge", challenge);
            } catch (Exception e) {
                log.warn("获取题目基本信息失败，跳过: {}", e.getMessage());
            }
        }

        infoMap.put("version", version);
        infoMap.put("draft", draft);

        // 若有容器镜像，记录pullAddress列表（供无Docker环境时参考）
        if (version.getChallengeId() != null) {
            try {
                List<ChallengeContainerImageVo> images =
                    challengeContainerImageService.getByChallengeId(version.getChallengeId());
                if (images != null && !images.isEmpty()) {
                    infoMap.put("containerImages", images);
                }
            } catch (Exception e) {
                log.warn("获取容器镜像信息失败，跳过: {}", e.getMessage());
            }
        }

        String json = JsonUtils.toJsonString(infoMap);
        byte[] jsonBytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        ZipEntry entry = new ZipEntry(zipRootDir + "challenge_info.json");
        zos.putNextEntry(entry);
        zos.write(jsonBytes);
        zos.closeEntry();

        log.debug("已写入 challenge_info.json，大小: {} bytes", jsonBytes.length);
    }

    /**
     * 从OSS下载附件，先落盘到临时文件，再写入ZIP对应目录。
     * <p>适用于可能较大的附件：先完整落盘可以准确获取文件大小，
     * 并避免在写ZIP时长时间持有OSS连接。临时文件在写入ZIP后立即删除。</p>
     *
     * @param zos         ZIP输出流
     * @param dirInZip    ZIP中的目录路径（含末尾斜杠）
     * @param attachments 附件列表
     */
    private void downloadAttachmentsViaTempFile(ZipOutputStream zos, String dirInZip,
                                                List<DraftConfig.Attachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        for (DraftConfig.Attachment attachment : attachments) {
            ChallengeFileVo fileVo = resolveAttachmentFile(attachment);
            if (fileVo == null) {
                continue;
            }

            String entryName = buildEntryName(dirInZip, fileVo);

            Path tmpAttachment = null;
            try {
                // 1. 落盘到临时文件
                tmpAttachment = Files.createTempFile("cch_attach_" + fileVo.getId() + "_", null);
                OssClient ossClient = OssFactory.instance(fileVo.getService());
                try (OutputStream tmpOut = Files.newOutputStream(tmpAttachment)) {
                    ossClient.download(fileVo.getFileName(), tmpOut, contentLength -> {
                    });
                }

                // 2. 将临时文件写入ZIP
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                Files.copy(tmpAttachment, zos);
                zos.closeEntry();
                log.debug("已写入附件（落盘）: {}", entryName);
            } catch (Exception e) {
                log.warn("下载附件失败，跳过，fileId: {}, 文件名: {}, 错误: {}",
                    fileVo.getId(), fileVo.getFileName(), e.getMessage());
                // 关闭当前可能已打开的entry，避免ZIP损坏
                try {
                    zos.closeEntry();
                } catch (IOException ignored) {
                }
            } finally {
                // 立即删除临时文件，不等ZIP关闭
                if (tmpAttachment != null) {
                    try {
                        Files.deleteIfExists(tmpAttachment);
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    /**
     * 从OSS下载附件/WP文件并流式写入ZIP对应目录（适用于较小文件，如WP）
     *
     * @param zos         ZIP输出流
     * @param dirInZip    ZIP中的目录路径（含末尾斜杠）
     * @param attachments 附件列表
     */
    private void downloadAttachments(ZipOutputStream zos, String dirInZip,
                                     List<DraftConfig.Attachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        for (DraftConfig.Attachment attachment : attachments) {
            ChallengeFileVo fileVo = resolveAttachmentFile(attachment);
            if (fileVo == null) {
                continue;
            }

            String entryName = buildEntryName(dirInZip, fileVo);

            try {
                OssClient ossClient = OssFactory.instance(fileVo.getService());
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                ossClient.download(fileVo.getFileName(), zos, contentLength -> {
                });
                zos.closeEntry();
                log.debug("已写入附件: {}", entryName);
            } catch (Exception e) {
                log.warn("下载附件失败，跳过，fileId: {}, 文件名: {}, 错误: {}",
                    fileVo.getId(), fileVo.getFileName(), e.getMessage());
                // 关闭当前可能已打开的entry，避免ZIP损坏
                try {
                    zos.closeEntry();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 解析附件对象，校验 fileId 并查询文件记录。
     * <p>两个 downloadAttachments 方法共用的前置逻辑，避免重复代码。</p>
     *
     * @param attachment 附件对象
     * @return 文件记录VO，校验失败或记录不存在时返回 null
     */
    private ChallengeFileVo resolveAttachmentFile(DraftConfig.Attachment attachment) {
        if (StringUtils.isBlank(attachment.getFileId())) {
            log.warn("附件 fileId 为空，跳过: {}", attachment.getFileName());
            return null;
        }

        Long fileId;
        try {
            fileId = Long.parseLong(attachment.getFileId());
        } catch (NumberFormatException e) {
            log.warn("附件 fileId 格式错误，跳过: {}", attachment.getFileId());
            return null;
        }

        ChallengeFileVo fileVo = challengeFileService.queryById(fileId);
        if (fileVo == null) {
            log.warn("附件文件记录不存在，跳过，fileId: {}", fileId);
            return null;
        }

        return fileVo;
    }

    /**
     * 根据目录路径和文件记录构建 ZIP entry 名称。
     * <p>对文件名做 sanitize，防止路径遍历（ZIP Slip）攻击。</p>
     *
     * @param dirInZip ZIP中的目录路径（含末尾斜杠）
     * @param fileVo   文件记录VO
     * @return 安全的 ZIP entry 名称
     */
    private String buildEntryName(String dirInZip, ChallengeFileVo fileVo) {
        String rawName = StringUtils.isNotBlank(fileVo.getOriginalName())
            ? fileVo.getOriginalName()
            : fileVo.getFileName();
        // sanitizeFilename 会移除所有路径分隔符，彻底防止 ZIP Slip
        return dirInZip + sanitizeFilename(rawName);
    }

    /**
     * 从Registry拉取容器镜像并以gzip压缩的tar格式写入ZIP
     *
     * @param zos         ZIP输出流
     * @param dirInZip    ZIP中的目录路径（含末尾斜杠）
     * @param challengeId 题目ID
     */
    private void exportContainerImages(ZipOutputStream zos, String dirInZip, Long challengeId) {
        ContainerClient containerClient = cchContainerConfigService.getActiveClient();
        if (containerClient == null) {
            log.info("无活跃容器客户端连接，跳过镜像导出（pullAddress已记录在 challenge_info.json 中）");
            return;
        }

        List<ChallengeContainerImageVo> images;
        try {
            images = challengeContainerImageService.getByChallengeId(challengeId);
        } catch (Exception e) {
            log.warn("获取题目容器镜像列表失败，跳过镜像导出: {}", e.getMessage());
            return;
        }

        if (images == null || images.isEmpty()) {
            log.debug("题目 {} 无容器镜像，跳过镜像导出", challengeId);
            return;
        }

        for (ChallengeContainerImageVo image : images) {
            if (!"available".equals(image.getStatus())) {
                log.debug("镜像状态非 available，跳过: id={}, status={}", image.getId(), image.getStatus());
                continue;
            }

            String pullAddress = image.getPullAddress();
            if (StringUtils.isBlank(pullAddress)) {
                log.warn("镜像 pullAddress 为空，跳过: id={}", image.getId());
                continue;
            }

            // 安全校验：镜像地址只允许合法字符，防止注入恶意地址
            if (pullAddress.length() > MAX_PULL_ADDRESS_LENGTH || !PULL_ADDRESS_PATTERN.matcher(pullAddress).matches()) {
                log.warn("镜像 pullAddress 格式非法，跳过: id={}, pullAddress={}", image.getId(), pullAddress);
                continue;
            }

            // 生成安全的文件名：将 registry/repo/name:tag 中的特殊字符替换为下划线
            String safeImageFileName = pullAddress.replaceAll("[/:@]", "_") + ".tar.gz";
            String entryName = dirInZip + safeImageFileName;

            log.info("开始导出镜像: {} -> {}", pullAddress, entryName);

            try {
                // 1. 从Registry拉取镜像到本地Docker
                containerClient.pullImage(pullAddress);

                // 2. 将镜像save为tar流，用GZIPOutputStream压缩后写入ZIP
                try (java.io.InputStream imageStream = containerClient.saveImage(pullAddress)) {
                    ZipEntry entry = new ZipEntry(entryName);
                    zos.putNextEntry(entry);

                    try (GZIPOutputStream gzipOut = new GZIPOutputStream(zos) {
                        // 重写close()，防止GZIPOutputStream.close()关闭底层ZipOutputStream
                        @Override
                        public void close() throws IOException {
                            finish();
                            out.flush();
                            // 不调用 super.close()，避免关闭 ZipOutputStream
                        }
                    }) {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;
                        while ((bytesRead = imageStream.read(buffer)) != -1) {
                            gzipOut.write(buffer, 0, bytesRead);
                        }
                        gzipOut.finish();
                    }

                    zos.closeEntry();
                    log.info("镜像导出成功: {}", pullAddress);
                }

                // 3. 清理本地拉取的镜像，释放空间
                try {
                    containerClient.removeImage(pullAddress);
                    log.debug("已清理本地镜像: {}", pullAddress);
                } catch (Exception cleanupEx) {
                    log.warn("清理本地镜像失败（不影响导出结果）: {}, 错误: {}", pullAddress, cleanupEx.getMessage());
                }

            } catch (Exception e) {
                log.warn("导出镜像失败，跳过: {}, 错误: {}", pullAddress, e.getMessage());
                // 关闭当前可能已打开的entry，避免ZIP损坏
                try {
                    zos.closeEntry();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 清理OSS缓存文件
     * <p>导出失败时调用，删除已上传但无效的OSS文件，避免产生垃圾文件。</p>
     *
     * @param ossFileId   OSS文件ID（可为null，为null时跳过）
     * @param ossFileName OSS文件名（仅用于日志）
     */
    private void cleanupOssFile(Long ossFileId, String ossFileName) {
        if (ossFileId == null) {
            return;
        }
        try {
            sysOssService.deleteWithValidByIds(List.of(ossFileId), false);
            log.info("已清理导出缓存文件，OSS文件ID: {}, 文件名: {}", ossFileId, ossFileName);
        } catch (Exception ex) {
            log.warn("清理导出缓存文件失败（不影响任务状态更新），OSS文件ID: {}, 文件名: {}, 错误: {}",
                ossFileId, ossFileName, ex.getMessage());
        }
    }

    /**
     * 获取文件保留时间（小时）
     */
    private int getFileRetentionHours() {
        try {
            String value = sysConfigService.selectConfigByKey(CONFIG_KEY_FILE_RETENTION_HOURS);
            if (StringUtils.isNotBlank(value)) {
                int hours = Integer.parseInt(value);
                // 验证配置值的合理性（1-720小时，即1小时到30天）
                if (hours < 1 || hours > 720) {
                    log.warn("文件保留时间配置值不合理: {} 小时，使用默认值: {} 小时", hours, DEFAULT_RETENTION_HOURS);
                    return DEFAULT_RETENTION_HOURS;
                }
                return hours;
            }
        } catch (NumberFormatException e) {
            log.warn("文件保留时间配置格式错误，使用默认值: {} 小时", DEFAULT_RETENTION_HOURS, e);
        } catch (Exception e) {
            log.warn("获取文件保留时间配置失败，使用默认值: {} 小时", DEFAULT_RETENTION_HOURS, e);
        }
        return DEFAULT_RETENTION_HOURS;
    }

    /**
     * 清理文件名，移除不安全字符，防止路径遍历攻击（ZIP Slip）
     * <p>会移除所有路径分隔符（{@code /}、{@code \}）及其他危险字符，
     * 确保返回值不含任何目录层级，可安全用于 ZIP entry 名称的文件名部分。</p>
     *
     * @param filename 原始文件名
     * @return 清理后的安全文件名
     */
    private String sanitizeFilename(String filename) {
        if (StringUtils.isBlank(filename)) {
            return "unknown";
        }

        // 移除路径分隔符和其他危险字符（包括 . 开头的隐藏文件前缀也保留，但 .. 会被处理）
        String sanitized = filename.replaceAll("[\\\\/:*?\"<>|]", "_");

        // 额外防御：移除 .. 序列，彻底杜绝路径遍历
        sanitized = sanitized.replaceAll("\\.{2,}", "_");

        // 限制文件名长度（防止过长文件名）
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }

        // 如果清理后为空，使用默认值
        if (StringUtils.isBlank(sanitized)) {
            sanitized = "unknown";
        }

        return sanitized;
    }
}
