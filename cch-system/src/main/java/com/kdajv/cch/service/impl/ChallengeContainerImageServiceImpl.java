package com.kdajv.cch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kdajv.cch.domain.ChallengeContainerImage;
import com.kdajv.cch.domain.bo.ChallengeContainerImageBo;
import com.kdajv.cch.domain.vo.ChallengeContainerImageVo;
import com.kdajv.cch.mapper.ChallengeContainerImageMapper;
import com.kdajv.cch.service.IChallengeContainerImageService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.oss.core.OssClient;
import org.dromara.common.oss.factory.OssFactory;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * 挑战容器镜像Service业务层处理
 *
 * @author Lingma
 * @date 2026-01-27
 */
@Service
@RequiredArgsConstructor
public class ChallengeContainerImageServiceImpl implements IChallengeContainerImageService {

    private final ChallengeContainerImageMapper baseMapper;

    private static final Logger log = LoggerFactory.getLogger(ChallengeContainerImageServiceImpl.class);

    /**
     * 查询挑战容器镜像
     */
    @Override
    public ChallengeContainerImageVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询挑战容器镜像列表
     */
    @Override
    public TableDataInfo<ChallengeContainerImageVo> queryPageList(ChallengeContainerImageBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ChallengeContainerImage> lqw = buildQueryWrapper(bo);
        Page<ChallengeContainerImageVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的挑战容器镜像列表
     */
    @Override
    public List<ChallengeContainerImageVo> queryList(ChallengeContainerImageBo bo) {
        LambdaQueryWrapper<ChallengeContainerImage> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 新增挑战容器镜像
     */
    @Override
    public Boolean insertByBo(ChallengeContainerImageBo bo) {
        ChallengeContainerImage add = new ChallengeContainerImage();
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
        org.springframework.beans.BeanUtils.copyProperties(bo, add);
        validEntityBeforeSave(add);
        return baseMapper.insert(add) > 0;
    }

    /**
     * 修改挑战容器镜像
     */
    @Override
    public Boolean updateByBo(ChallengeContainerImageBo bo) {
        ChallengeContainerImage update = new ChallengeContainerImage();
        org.springframework.beans.BeanUtils.copyProperties(bo, update);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 修改挑战容器镜像状态
     */
    @Override
    public Boolean updateStatus(Long id, String status, Double progress, String errorMessage) {
        ChallengeContainerImage update = new ChallengeContainerImage();
        update.setId(id);
        update.setStatus(status);
        if (progress != null) {
            update.setProgress(java.math.BigDecimal.valueOf(progress));
        }
        update.setErrorMessage(errorMessage);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ChallengeContainerImage entity) {
        // 做一些数据校验
    }

    /**
     * 批量删除挑战容器镜像
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            // 获取要删除的记录信息
            List<ChallengeContainerImageVo> imagesToDelete = baseMapper.selectVoList(new LambdaQueryWrapper<ChallengeContainerImage>().in(ChallengeContainerImage::getId, ids));

            // 尝试删除OSS中的文件
            for (ChallengeContainerImageVo image : imagesToDelete) {
                try {
                    if (StringUtils.isNotBlank(image.getFilePath())) {
                        OssClient ossClient = OssFactory.instance();
                        ossClient.delete(image.getFilePath());
                        log.info("已删除OSS中的镜像文件: {}", image.getFilePath());
                    }
                } catch (Exception e) {
                    log.error("删除OSS文件失败: {}", image.getFilePath(), e);
                    // 继续删除其他文件，不中断删除流程
                }
            }
        }

        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<ChallengeContainerImage> buildQueryWrapper(ChallengeContainerImageBo bo) {
        LambdaQueryWrapper<ChallengeContainerImage> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, ChallengeContainerImage::getId, bo.getId());
        lqw.eq(bo.getChallengeId() != null, ChallengeContainerImage::getChallengeId, bo.getChallengeId());
        lqw.like(StringUtils.isNotBlank(bo.getImageName()), ChallengeContainerImage::getImageName, bo.getImageName());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), ChallengeContainerImage::getStatus, bo.getStatus());
        lqw.orderByDesc(ChallengeContainerImage::getCreateTime);
        return lqw;
    }

    @Override
    public List<ChallengeContainerImageVo> getByChallengeId(Long challengeId) {
        LambdaQueryWrapper<ChallengeContainerImage> lqw = Wrappers.lambdaQuery();
        lqw.eq(ChallengeContainerImage::getChallengeId, challengeId);
        lqw.orderByDesc(ChallengeContainerImage::getCreateTime);
        return baseMapper.selectVoList(lqw);
    }

    @Override
    public Boolean deleteByChallengeId(Long challengeId) {
        LambdaQueryWrapper<ChallengeContainerImage> lqw = Wrappers.lambdaQuery();
        lqw.eq(ChallengeContainerImage::getChallengeId, challengeId);
        return baseMapper.delete(lqw) > 0;
    }
}
