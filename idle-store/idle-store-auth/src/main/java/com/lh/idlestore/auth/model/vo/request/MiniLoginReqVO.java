package com.lh.idlestore.auth.model.vo.request;

import com.lh.framework.common.validation.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小程序端手机号登陆
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MiniLoginReqVO {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @PhoneNumber
    private String phone;
}
