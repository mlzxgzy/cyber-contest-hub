package com.kdajv.cch.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
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
     */
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
}
