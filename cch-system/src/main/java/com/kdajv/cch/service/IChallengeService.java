package com.kdajv.cch.service;

import com.kdajv.cch.domain.bo.ChallengeBo;
import com.kdajv.cch.domain.bo.ChallengeDraftBo;
import com.kdajv.cch.domain.vo.ChallengeDraftVo;
import com.kdajv.cch.domain.vo.ChallengeVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 题目列表Service接口
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
public interface IChallengeService {

    /**
     * 查询题目列表
     *
     * @param id 主键
     * @return 题目列表
     */
    ChallengeVo queryById(Long id);

    /**
     * 分页查询题目列表列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 题目列表分页列表
     */
    TableDataInfo<ChallengeVo> queryPageList(ChallengeBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的题目列表列表
     *
     * @param bo 查询条件
     * @return 题目列表列表
     */
    List<ChallengeVo> queryList(ChallengeBo bo);

    /**
     * 新增题目列表
     *
     * @param bo 题目列表
     * @return 是否新增成功
     */
    Boolean insertByBo(ChallengeBo bo);

    /**
     * 修改题目列表
     *
     * @param bo 题目列表
     * @return 是否修改成功
     */
    Boolean updateByBo(ChallengeBo bo);

    /**
     * 校验并批量删除题目列表信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 初始化一道新题目（含首个草稿），用于"新增题目入库"一体化流程
     * <p>
     * 在一个事务内创建 t_challenge 与 t_challenge_draft，
     * 避免传统"先建骨架、再进编辑页填内容"的两步式操作。
     *
     * @param bo 题目基础信息 + 初始草稿配置
     * @return 初始化的草稿信息（含新题目ID）
     */
    ChallengeDraftVo initChallengeWithDraft(ChallengeDraftBo bo);

    /**
     * 获取全部知识点标签（缓存，用于搜索下拉）
     *
     * @return 去重排序后的知识点标签列表
     */
    List<String> listKnowledgeTags();

    /**
     * 刷新知识点标签缓存（扫描全部草稿 config 聚合）
     */
    void refreshKnowledgeTags();
}
