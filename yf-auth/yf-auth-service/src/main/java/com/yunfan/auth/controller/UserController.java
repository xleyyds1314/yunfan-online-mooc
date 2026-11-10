package com.yunfan.auth.controller;

import com.yunfan.api.dto.user.StudentFormDTO;
import com.yunfan.auth.service.ICodeService;
import com.yunfan.auth.service.IUserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @Author: fsq
 * @Date: 2025/5/20 13:33
 * @Version: 1.0
 */
@RestController
@RequestMapping("users")
@RequiredArgsConstructor
@ApiIgnore
public class UserController {

    private final IUserService userService;

    @ApiOperation("学员通过手机验证码注册")
    @PostMapping("/register")
    public void registerByCellphone(@RequestBody StudentFormDTO dto){
         userService.register(dto);
    }

    @ApiOperation("找回密码")
    @PostMapping("/reset")
    public void updateMyPassword(@RequestBody StudentFormDTO dto){
        userService.updateMyPassword(dto);
    }

}
