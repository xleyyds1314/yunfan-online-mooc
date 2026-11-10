package com.yunfan.promotion.service;

import com.yunfan.api.dto.promotion.CouponDetailSimpleVO;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.promotion.domain.dto.CouponFormDTO;
import com.yunfan.promotion.domain.dto.CouponIssueFormDTO;
import com.yunfan.promotion.domain.po.Coupon;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.promotion.domain.query.CouponQuery;
import com.yunfan.promotion.domain.vo.CouponDetailVO;
import com.yunfan.promotion.domain.vo.CouponPageVO;
import com.yunfan.promotion.domain.vo.CouponVO;

import java.util.List;

/**
 * <p>
 * 优惠券的规则信息 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2023-10-28
 */
public interface ICouponService extends IService<Coupon> {

    void saveCoupon(CouponFormDTO dto);

    PageDTO<CouponPageVO> queryCouponPage(CouponQuery query);

    void issueCoupon(Long id,CouponIssueFormDTO dto);

    void modifyCoupon(Long id,CouponFormDTO dto);

    CouponDetailVO queryCouponById(Long id);

    void pauseIssue(Long id);

    void deleteById(Long id);

    void beginIssueBatch(List<Coupon> records);

    List<CouponVO> queryIssuingCoupons();

    CouponDetailSimpleVO querySimpleCouponById(Long id);
}
