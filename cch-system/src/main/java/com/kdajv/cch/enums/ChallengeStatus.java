package com.kdajv.cch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题目状态枚举
 *
 * @author system
 * @date 2026-08-31
 */
@Getter
@AllArgsConstructor
public enum ChallengeStatus {

    /**
     * 草稿中（尚未发版入库）
     */
    DRAFT(0, "草稿中"),

    /**
     * 已入库（已发布版本）
     */
    PUBLISHED(1, "已入库"),

    /**
     * 已停用（禁止导入项目）
     */
    DISABLED(2, "已停用");

    private final int code;

    private final String desc;

    public static ChallengeStatus of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ChallengeStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
