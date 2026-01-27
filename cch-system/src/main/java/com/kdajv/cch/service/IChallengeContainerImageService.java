package com.kdajv.cch.service;

import com.kdajv.cch.domain.vo.ChallengeContainerImageVo;
import com.kdajv.cch.domain.bo.ChallengeContainerImageBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 挑战容器镜像Service接口
 *
 * @author Lingma
 * @date 2026-01-27
 */
public interface IChallengeContainerImageService {

    /**
     * 查询挑战容器镜像
     *
     * @param id 主键
     * @return 挑战容器镜像
     */
    ChallengeContainerImageVo queryById(Long id);

    /**
     * 分页查询挑战容器镜像列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 挑战容器镜像分页列表
     */
    TableDataInfo<ChallengeContainerImageVo> queryPageList(ChallengeContainerImageBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的挑战容器镜像列表
     *
     * @param bo 查询条件
     * @return 挑战容器镜像列表
     */
    List<ChallengeContainerImageVo> queryList(ChallengeContainerImageBo bo);

    /**
     * 新增挑战容器镜像
     *
     * @param bo 挑战容器镜像
     * @return 是否新增成功
     */
    Boolean insertByBo(ChallengeContainerImageBo bo);

    /**
     * 修改挑战容器镜像
     *
     * @param bo 挑战容器镜像
     * @return 是否修改成功
     */
    Boolean updateByBo(ChallengeContainerImageBo bo);

    /**
     * 修改挑战容器镜像状态
     *
     * @param id     主键
     * @param status 状态
     * @param progress 进度
     * @param errorMessage 错误信息
     * @return 是否修改成功
     */
    Boolean updateStatus(Long id, String status, Double progress, String errorMessage);

    /**
     * 批量删除挑战容器镜像
     *
     * @param ids 需要删除的主键集合
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 根据题目ID查询镜像列表
     *
     * @param challengeId 题目ID
     * @return 镜像列表
     */
    List<ChallengeContainerImageVo> getByChallengeId(Long challengeId);

    /**
     * 根据题目ID删除镜像
     *
     * @param challengeId 题目ID
     * @return 是否删除成功
     */
    Boolean deleteByChallengeId(Long challengeId);
}