package com.yunfan.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.promotion.domain.dto.CouponDiscountDTO;
import com.yunfan.promotion.domain.dto.OrderCouponDTO;

import java.util.List;

public interface IDiscountService {

    CouponDiscountDTO queryDiscountDetailByOrder(OrderCouponDTO orderCouponDTO);
}