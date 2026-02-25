package com.kdajv.cch.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 通过邀请加入项目请求对象
 */
@Data
public class ProjectJoinByInviteBo {

    /**
     * 邀请Code
     */
    @NotBlank(message = "邀请Code不能为空")
    private String inviteCode;
}

