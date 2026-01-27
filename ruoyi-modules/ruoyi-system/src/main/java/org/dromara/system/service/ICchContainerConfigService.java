package org.dromara.system.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.vo.CchContainerConfigVo;

import java.util.List;

/**
 * 容器配置 服务层
 *
 * @author system
 * @date 2025-12-11
 */
public interface ICchContainerConfigService {

    /**
     * 分页查询容器配置列表
     *
     * @param configName 配置名称（可选）
     * @param backendType 后端类型（可选）
     * @param pageQuery 分页参数
     * @return 容器配置分页列表
     */
    TableDataInfo<CchContainerConfigVo> queryPageList(String configName, String backendType, PageQuery pageQuery);

    /**
     * 查询容器配置信息
     *
     * @param configId 容器配置ID
     * @return 容器配置信息
     */
    CchContainerConfigVo queryById(Long configId);

    /**
     * 查询容器配置列表
     *
     * @param configName 配置名称（可选）
     * @param backendType 后端类型（可选）
     * @return 容器配置集合
     */
    List<CchContainerConfigVo> queryList(String configName, String backendType);

    /**
     * 新增容器配置
     *
     * @param vo 容器配置信息
     * @return 结果
     */
    Boolean insertByVo(CchContainerConfigVo vo);

    /**
     * 修改容器配置
     *
     * @param vo 容器配置信息
     * @return 结果
     */
    Boolean updateByVo(CchContainerConfigVo vo);

    /**
     * 批量删除容器配置
     *
     * @param ids 需要删除的容器配置ID
     * @return 结果
     */
    Boolean deleteByIds(List<Long> ids);

    /**
     * 测试连接
     *
     * @param id 容器配置ID
     * @return 结果
     */
    Boolean testConnection(Long id);

    /**
     * 获取当前活跃的容器实例
     *
     * @return 活跃的容器配置，如果不存在返回null
     */
    CchContainerConfigVo getActiveInstance();

    /**
     * 断开当前活跃的容器实例
     */
    void disconnectActiveInstance();

}
