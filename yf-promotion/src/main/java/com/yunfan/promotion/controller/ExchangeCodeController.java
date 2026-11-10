package com.yunfan.promotion.controller;


import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.promotion.domain.po.ExchangeCode;
import com.yunfan.promotion.domain.query.CodeQuery;
import com.yunfan.promotion.domain.vo.ExchangeCodeVO;
import com.yunfan.promotion.service.IExchangeCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 兑换码 前端控制器
 * </p>
 *
 * @author xleyyds1314
 * @since 2023-10-28
 */
@Api(tags = "兑换码相关接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/codes")
public class ExchangeCodeController {

    private final IExchangeCodeService codeService;

    @GetMapping("page")
    @ApiOperation("查询兑换码")
    public PageDTO<ExchangeCodeVO> queryCodePage(CodeQuery query){
        return codeService.queryCodePage(query);
    }

}
