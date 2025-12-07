package com.kdajv.cch.service.impl;

import com.kdajv.cch.domain.ChallengeFileExt;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.oss.core.OssClient;
import org.dromara.common.oss.entity.UploadResult;
import org.dromara.common.oss.enums.AccessPolicyType;
import org.dromara.common.oss.factory.OssFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import com.kdajv.cch.domain.bo.ChallengeFileBo;
import com.kdajv.cch.domain.vo.ChallengeFileVo;
import com.kdajv.cch.domain.ChallengeFile;
import com.kdajv.cch.mapper.ChallengeFileMapper;
import com.kdajv.cch.service.IChallengeFileService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 题目文件Service业务层处理
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ChallengeFileServiceImpl implements IChallengeFileService {

    private final ChallengeFileMapper baseMapper;

    /**
     * 查询题目文件
     *
     * @param id 主键
     * @return 题目文件
     */
    @Override
    public ChallengeFileVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询题目文件列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 题目文件分页列表
     */
    @Override
    public TableDataInfo<ChallengeFileVo> queryPageList(ChallengeFileBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ChallengeFile> lqw = buildQueryWrapper(bo);
        Page<ChallengeFileVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的题目文件列表
     *
     * @param bo 查询条件
     * @return 题目文件列表
     */
    @Override
    public List<ChallengeFileVo> queryList(ChallengeFileBo bo) {
        LambdaQueryWrapper<ChallengeFile> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ChallengeFile> buildQueryWrapper(ChallengeFileBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ChallengeFile> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(ChallengeFile::getId);
        lqw.eq(bo.getChallengeId() != null, ChallengeFile::getChallengeId, bo.getChallengeId());
        lqw.like(StringUtils.isNotBlank(bo.getFileName()), ChallengeFile::getFileName, bo.getFileName());
        lqw.like(StringUtils.isNotBlank(bo.getOriginalName()), ChallengeFile::getOriginalName, bo.getOriginalName());
        lqw.eq(StringUtils.isNotBlank(bo.getFileSuffix()), ChallengeFile::getFileSuffix, bo.getFileSuffix());
        lqw.eq(StringUtils.isNotBlank(bo.getUrl()), ChallengeFile::getUrl, bo.getUrl());
        lqw.eq(StringUtils.isNotBlank(bo.getService()), ChallengeFile::getService, bo.getService());
        return lqw;
    }

    /**
     * 新增题目文件
     *
     * @param bo 题目文件
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(ChallengeFileBo bo) {
        ChallengeFile add = MapstructUtils.convert(bo, ChallengeFile.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改题目文件
     *
     * @param bo 题目文件
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(ChallengeFileBo bo) {
        ChallengeFile update = MapstructUtils.convert(bo, ChallengeFile.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ChallengeFile entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除题目文件信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 上传 MultipartFile 到对象存储服务，并保存文件信息到数据库
     *
     * @param challengeId
     * @param file        要上传的 MultipartFile 对象
     * @return 上传成功后的 SysOssVo 对象，包含文件信息
     * @throws ServiceException 如果上传过程中发生异常，则抛出 ServiceException 异常
     */
    @Override
    public ChallengeFileVo upload(Long challengeId, MultipartFile file) {
        String originalfileName = file.getOriginalFilename();
        String suffix = StringUtils.substring(originalfileName, originalfileName.lastIndexOf("."), originalfileName.length());
        OssClient storage = OssFactory.instance();
        UploadResult uploadResult;
        try {
            uploadResult = storage.uploadSuffix(file.getBytes(), suffix, file.getContentType());
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
        ChallengeFileExt ext1 = new ChallengeFileExt();
        ext1.setFileSize(file.getSize());
        ext1.setContentType(file.getContentType());
        // 保存文件信息
        return buildResultEntity(challengeId, originalfileName, suffix, storage.getConfigKey(), uploadResult, ext1);
    }

    @NotNull
    private ChallengeFileVo buildResultEntity(Long challengeId, String originalfileName, String suffix, String configKey, UploadResult uploadResult, ChallengeFileExt ext1) {
        ChallengeFile challengeFile = new ChallengeFile();
        challengeFile.setChallengeId(challengeId);
        challengeFile.setUrl(uploadResult.getUrl());
        challengeFile.setFileSuffix(suffix);
        challengeFile.setFileName(uploadResult.getFilename());
        challengeFile.setOriginalName(originalfileName);
        challengeFile.setService(configKey);
        challengeFile.setExt1(JsonUtils.toJsonString(ext1));
        baseMapper.insert(challengeFile);
        ChallengeFileVo challengeFileVo = MapstructUtils.convert(challengeFile, ChallengeFileVo.class);
        return this.matchingUrl(challengeFileVo);
    }

    /**
     * 桶类型为 private 的URL 修改为临时URL时长为120s
     *
     * @param challengeFileVo OSS对象
     * @return challengeFileVo 匹配Url的OSS对象
     */
    private ChallengeFileVo matchingUrl(ChallengeFileVo challengeFileVo) {
        OssClient storage = OssFactory.instance(challengeFileVo.getService());
        // 仅修改桶类型为 private 的URL，临时URL时长为120s
        if (AccessPolicyType.PRIVATE == storage.getAccessPolicy()) {
            challengeFileVo.setUrl(storage.getPrivateUrl(challengeFileVo.getFileName(), Duration.ofSeconds(120)));
        }
        return challengeFileVo;
    }
}
