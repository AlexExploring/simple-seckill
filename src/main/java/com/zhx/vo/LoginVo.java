package com.zhx.vo;

import com.zhx.validator.IsMobile;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LoginVo {

    @NotNull
    @IsMobile
    private String mobile;

    @NotNull
    private String password;
}
