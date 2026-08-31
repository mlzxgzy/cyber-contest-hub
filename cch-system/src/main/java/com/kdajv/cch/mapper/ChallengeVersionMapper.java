package com.kdajv.cch.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kdajv.cch.domain.ChallengeVersion;
import com.kdajv.cch.domain.vo.ChallengeVersionVo;
import org.dromara.common.mybatis.annotation.DataColumn;
import org.dromara.common.mybatis.annotation.DataPermission;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 题目版本Mapper接口
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
public interface ChallengeVersionMapper extends BaseMapperPlus<ChallengeVersion, ChallengeVersionVo> {

    /**
     * 分页查询题目版本列表，并进行数据权限控制
     *
     * @param page         分页参数
     * @param queryWrapper 查询条件
     * @return 分页的题目版本信息
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default Page<ChallengeVersionVo> selectPageVersionList(Page<ChallengeVersion> page, Wrapper<ChallengeVersion> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

    /**
     * 查询题目版本列表，并进行数据权限控制
     *
     * @param queryWrapper 查询条件
     * @return 题目版本信息集合
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default List<ChallengeVersionVo> selectVersionList(Wrapper<ChallengeVersion> queryWrapper) {
        return this.selectVoList(queryWrapper);
    }
}
