package com.kdajv.cch.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kdajv.cch.domain.ChallengeDraft;
import com.kdajv.cch.domain.vo.ChallengeDraftVo;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 题目草稿Mapper接口
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
public interface ChallengeDraftMapper extends BaseMapperPlus<ChallengeDraft, ChallengeDraftVo> {

    /**
     * 分页查询题目草稿列表，并进行数据权限控制
     *
     * @param page         分页参数
     * @param queryWrapper 查询条件
     * @return 分页的题目草稿信息
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default Page<ChallengeDraftVo> selectPageDraftList(Page<ChallengeDraft> page, Wrapper<ChallengeDraft> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

    /**
     * 查询题目草稿列表，并进行数据权限控制
     *
     * @param queryWrapper 查询条件
     * @return 题目草稿信息集合
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default List<ChallengeDraftVo> selectDraftList(Wrapper<ChallengeDraft> queryWrapper) {
        return this.selectVoList(queryWrapper);
    }

    /**
     * 查询全部草稿的知识点 JSON 数组（仅拉取 config.knowledge 字段，避免全量 config 传输）
     *
     * @return 每行一个 knowledge JSON 数组字符串（如 ["web","pwn"]），无知识点时为 null
     */
    @Select("""
        SELECT json_extract(config, '$.knowledge')
        FROM t_challenge_draft
        WHERE del_flag = 0
        """)
    List<String> selectKnowledgeJsonList();

}
