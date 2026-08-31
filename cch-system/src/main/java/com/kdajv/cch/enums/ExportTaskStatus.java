package com.kdajv.cch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 导出任务状态枚举
 *
 * @author system
 * @date 2026-08-31
 */
@Getter
@AllArgsConstructor
public enum ExportTaskStatus {

    /**
     * 待处理
     */
    PENDING(0, "待处理"),

    /**
     * 处理中
     */
    PROCESSING(1, "处理中"),

    /**
     * 已完成
     */
    COMPLETED(2, "已完成"),

    /**
     * 失败
     */
    FAILED(3, "失败");

    private final int code;

    private final String desc;

    public static ExportTaskStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ExportTaskStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
