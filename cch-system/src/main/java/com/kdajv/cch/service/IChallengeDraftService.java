package com.kdajv.cch.service;

import com.kdajv.cch.domain.bo.ChallengeDraftBo;
import com.kdajv.cch.domain.vo.ChallengeDraftVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 题目草稿Service接口
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
public interface IChallengeDraftService {

    /**
     * 查询题目草稿
     *
     * @param id 主键
     * @return 题目草稿
     */
    ChallengeDraftVo queryById(Long id);

    /**
     * 分页查询题目草稿列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 题目草稿分页列表
     */
    TableDataInfo<ChallengeDraftVo> queryPageList(ChallengeDraftBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的题目草稿列表
     *
     * @param bo 查询条件
     * @return 题目草稿列表
     */
    List<ChallengeDraftVo> queryList(ChallengeDraftBo bo);

    /**
     * 查询最新的题目草稿
     *
     * @param challengeId 题目ID
     * @return 最新的题目草稿
     */
    ChallengeDraftVo queryTop1ByChallengeIdOrderByCreateTimeDesc(Long challengeId);

    /**
     * 新增题目草稿
     *
     * @param bo 题目草稿
     * @return 是否新增成功
     */
    Boolean insertByBo(ChallengeDraftBo bo);

    /**
     * 修改题目草稿
     *
     * @param bo 题目草稿
     * @return 新增后的草稿数据（每次保存生成新记录）
     */
    ChallengeDraftVo updateByBo(ChallengeDraftBo bo);

    /**
     * 校验并批量删除题目草稿信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 查询题目草稿的版本历史列表（用于构建树状结构）
     *
     * @param challengeId 题目ID
     * @return 题目草稿版本列表（按创建时间倒序）
     */
    List<ChallengeDraftVo> queryHistoryListByChallengeId(Long challengeId);

    /**
     * 从指定版本派生新版本
     *
     * @param parentDraftId 父版本草稿ID
     * @return 派生后的新草稿数据
     */
    ChallengeDraftVo forkFromDraftId(Long parentDraftId);
}
