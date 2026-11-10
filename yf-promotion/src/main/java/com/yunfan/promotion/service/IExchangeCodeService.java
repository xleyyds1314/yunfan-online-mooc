package com.yunfan.promotion.service;

import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.promotion.domain.po.Coupon;
import com.yunfan.promotion.domain.po.ExchangeCode;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.promotion.domain.query.CodeQuery;
import com.yunfan.promotion.domain.vo.ExchangeCodeVO;

/**
 * <p>
 * 兑换码 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2023-10-28
 */
public interface IExchangeCodeService extends IService<ExchangeCode> {

    //异步生成兑换码
    void asyncGenerateExchangeCode(Coupon coupon);

    PageDTO<ExchangeCodeVO> queryCodePage(CodeQuery query);

    boolean updateExchangeCodeMark(long serialNum, boolean b);

    Long exchangeTargetId(long serialNum);
}
