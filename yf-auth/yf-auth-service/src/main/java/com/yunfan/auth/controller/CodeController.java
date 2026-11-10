package com.yunfan.auth.controller;/**
 * @author xleyyds1314
 * @date 2025/5/20 9:59
 */

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import com.yunfan.auth.domain.po.Menu;
import com.yunfan.auth.domain.vo.MenuOptionVO;
import com.yunfan.auth.service.ICodeService;
import com.yunfan.auth.service.IMenuService;
import com.yunfan.common.ratelimiter.annotation.YfRateLimiter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.security.KeyPair;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: fsq
 * @Date: 2025/5/20 9:59
 * @Version: 1.0
 */
@RestController
@RequestMapping("code")
@RequiredArgsConstructor
@Api(tags = "发送短信验证码")
public class CodeController {

    private final ICodeService codeService;

    @PostMapping("/verifycode")
    @ApiOperation("注册发送短信验证码")
    @YfRateLimiter(permitsPerSecond = 3000, timeout = 0)
    public void sendVerfiycode(@RequestParam String cellPhone){
        codeService.sendVerifyCode(cellPhone);
    }
}
