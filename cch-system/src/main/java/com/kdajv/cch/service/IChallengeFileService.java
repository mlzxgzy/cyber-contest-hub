package com.kdajv.cch.service;

import com.kdajv.cch.domain.vo.ChallengeFileVo;
import com.kdajv.cch.domain.bo.ChallengeFileBo;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * 题目文件Service接口
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
public interface IChallengeFileService {

    /**
     * 查询题目文件
     *
     * @param id 主键
     * @return 题目文件
     */
    ChallengeFileVo queryById(Long id);

    /**
     * 分页查询题目文件列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 题目文件分页列表
     */
    TableDataInfo<ChallengeFileVo> queryPageList(ChallengeFileBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的题目文件列表
     *
     * @param bo 查询条件
     * @return 题目文件列表
     */
    List<ChallengeFileVo> queryList(ChallengeFileBo bo);

    /**
     * 新增题目文件
     *
     * @param bo 题目文件
     * @return 是否新增成功
     */
    Boolean insertByBo(ChallengeFileBo bo);

    /**
     * 修改题目文件
     *
     * @param bo 题目文件
     * @return 是否修改成功
     */
    Boolean updateByBo(ChallengeFileBo bo);

    /**
     * 校验并批量删除题目文件信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 上传 MultipartFile 到对象存储服务，并保存文件信息到数据库
     *
     * @param challengeId 赛题Id
     * @param file        要上传的 MultipartFile 对象
     * @return 上传成功后的 ChallengeFileVo 对象，包含文件信息
     * @throws ServiceException 如果上传过程中发生异常，则抛出 ServiceException 异常
     */
    ChallengeFileVo upload(Long challengeId, MultipartFile file);

    /**
     * 文件下载方法，支持一次性下载完整文件
     *
     * @param ossId    文件Id
     * @param response HttpServletResponse对象，用于设置响应头和向客户端发送文件内容
     */
    void download(Long ossId, HttpServletResponse response) throws IOException;
}
