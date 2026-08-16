package com.kdajv.cch.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.List;

/**
 * 题目配置对象
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
public class DraftConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置描述
     */
    private String stem;

    /**
     * 难度
     */
    private String difficulty;

    /**
     * 运行类型：static(静态题目)、container(容器题目)、vm(虚拟机题目)
     */
    private String runType;

    /**
     * 知识点
     */
    private List<String> knowledge;

    /**
     * 题目附件
     */
    private List<Attachment> attachments;

    /**
     * Writeup
     */
    private List<Attachment> writeups;

    /**
     * Flag列表
     */
    private List<Flag> flags;

    /**
     * 容器靶机配置（runType=container 时使用）
     */
    private List<ContainerTarget> containerTargets;

    /**
     * 题目附件
     */
    @Data
    public static class Attachment {
        /**
         * 文件ID
         */
        public String fileId;
        /**
         * 文件名
         */
        public String fileName;
        /**
         * 文件URL
         */
        public String fileUrl;
        /**
         * 文件描述
         */
        public String remark;
    }

    /**
     * Flag基类
     * 使用策略模式，为静态和动态flag提供解耦设计
     * <p>
     * 通过 @JsonTypeInfo/@JsonSubTypes 实现多态反序列化（按 JSON 中的 type 字段还原为
     * StaticFlag/DynamicFlag 子类），保证静态 Flag 的 content 等子类字段在草稿存取过程中不丢失。
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = StaticFlag.class, name = "static"),
        @JsonSubTypes.Type(value = DynamicFlag.class, name = "dynamic")
    })
    @Data
    public static class Flag implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * Flag类型：static(静态) 或 dynamic(动态)
         */
        private String type;

        /**
         * 分值（用于分值推荐）
         */
        private Integer score;

        /**
         * Flag描述（给选手查看的）
         */
        private String description;

        /**
         * Flag备注（仅后台可见）
         */
        private String remark;
    }

    /**
     * 静态Flag
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class StaticFlag extends Flag {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * Flag内容
         */
        private String content;

        public StaticFlag() {
            super.setType("static");
        }
    }

    /**
     * 动态Flag
     * 预留扩展空间，未来可以添加动态生成逻辑
     */
    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class DynamicFlag extends Flag {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 动态Flag生成规则/配置
         * 未来可以扩展为JSON对象，包含生成规则、模板等信息
         */
        private String generatorConfig;

        public DynamicFlag() {
            super.setType("dynamic");
        }
    }

    /**
     * 容器靶机配置
     */
    @Data
    public static class ContainerTarget implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 靶机名称（可自定义）
         */
        private String name;

        /**
         * 镜像ID（从本题已上传镜像中选择）
         */
        private Long imageId;

        /**
         * 镜像名称（可选缓存字段，用于展示）
         */
        private String imageName;

        /**
         * 环境变量（key: 变量名, value: 变量值）
         */
        private Map<String, String> env;

        /**
         * 开放端口（key: 端口名称, value: 端口配置）
         */
        private Map<String, PortConfig> ports;

        /**
         * 资源限制
         */
        private ResourceLimit resources;
    }

    /**
     * 端口配置
     */
    @Data
    public static class PortConfig implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 协议（tcp/udp 等）
         */
        private String protocol;

        /**
         * 内部端口
         */
        private Integer internalPort;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 资源限制
     */
    @Data
    public static class ResourceLimit implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * CPU 限制（millicores，例如 500 = 0.5 核）
         */
        private Integer cpuLimit;

        /**
         * 内存限制（MB）
         */
        private Integer memoryLimit;
    }
}
