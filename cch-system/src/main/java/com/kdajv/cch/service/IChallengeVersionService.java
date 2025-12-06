package com.kdajv.cch.service;

import com.kdajv.cch.domain.bo.ChallengeVersionBo;
import com.kdajv.cch.domain.vo.ChallengeVersionVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 题目版本Service接口
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
public interface IChallengeVersionService {

    /**
     * 查询题目版本
     *
     * @param id 主键
     * @return 题目版本
     */
    ChallengeVersionVo queryById(Long id);

    /**
     * 分页查询题目版本列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 题目版本分页列表
     */
    TableDataInfo<ChallengeVersionVo> queryPageList(ChallengeVersionBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的题目版本列表
     *
     * @param bo 查询条件
     * @return 题目版本列表
     */
    List<ChallengeVersionVo> queryList(ChallengeVersionBo bo);

    /**
     * 新增题目版本
     *
     * @param bo 题目版本
     * @return 是否新增成功
     */
    Boolean insertByBo(ChallengeVersionBo bo);

    /**
     * 修改题目版本
     *
     * @param bo 题目版本
     * @return 是否修改成功
     */
    Boolean updateByBo(ChallengeVersionBo bo);

    /**
     * 校验并批量删除题目版本信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
