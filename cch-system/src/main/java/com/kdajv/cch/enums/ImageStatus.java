package com.kdajv.cch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 容器镜像状态枚举
 *
 * @author system
 * @date 2026-08-31
 */
@Getter
@AllArgsConstructor
public enum ImageStatus {

    /**
     * 上传中
     */
    UPLOADING("uploading", "上传中"),

    /**
     * 已上传
     */
    UPLOADED("uploaded", "已上传"),

    /**
     * 验证中
     */
    VALIDATING("validating", "验证中"),

    /**
     * 可用
     */
    AVAILABLE("available", "可用"),

    /**
     * 错误
     */
    ERROR("error", "错误");

    private final String code;

    private final String desc;

    public static ImageStatus of(String code) {
        if (code == null) {
            return null;
        }
        for (ImageStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
