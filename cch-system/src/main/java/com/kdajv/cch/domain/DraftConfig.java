package com.kdajv.cch.domain;

import lombok.Data;

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
     * 题目附件
     */
    private List<Attachment> attachments;

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
}
