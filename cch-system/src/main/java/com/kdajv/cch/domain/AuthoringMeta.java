package com.kdajv.cch.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 出题meta信息DTO类（仅出题项目使用）
 *
 * @author Zyi Guo
 * @date 2026-09-04
 */
@Data
public class AuthoringMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 出题来源（'self'自己出, 'external'外采）
     */
    private String authorSource;

    /**
     * 外采单位（仅出题来源为外采时填写）
     */
    private String externalUnit;
}
