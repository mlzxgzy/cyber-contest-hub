package com.kdajv.cch.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kdajv.cch.domain.Challenge;
import com.kdajv.cch.domain.vo.ChallengeVo;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 题目列表Mapper接口
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
public interface ChallengeMapper extends BaseMapperPlus<Challenge, ChallengeVo> {

    /**
     * 分页查询题目列表，并进行数据权限控制
     *
     * @param page         分页参数
     * @param queryWrapper 查询条件
     * @return 分页的题目信息
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default Page<ChallengeVo> selectPageChallengeList(Page<Challenge> page, Wrapper<Challenge> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

    /**
     * 查询题目列表，并进行数据权限控制
     *
     * @param queryWrapper 查询条件
     * @return 题目信息集合
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default List<ChallengeVo> selectChallengeList(Wrapper<Challenge> queryWrapper) {
        return this.selectVoList(queryWrapper);
    }

}
