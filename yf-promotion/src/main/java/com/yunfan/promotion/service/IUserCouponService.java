package com.yunfan.promotion.service;

import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.promotion.domain.dto.CouponDiscountDTO;
import com.yunfan.promotion.domain.dto.OrderCouponDTO;
import com.yunfan.promotion.domain.dto.OrderCourseDTO;
import com.yunfan.promotion.domain.dto.UserCouponDTO;
import com.yunfan.promotion.domain.po.Coupon;
import com.yunfan.promotion.domain.po.UserCoupon;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.promotion.domain.query.UserCouponQuery;
import com.yunfan.promotion.domain.vo.CouponPageVO;
import com.yunfan.promotion.domain.vo.CouponVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户领取优惠券的记录，是真正使用的优惠券信息 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2023-10-29
 */
public interface IUserCouponService extends IService<UserCoupon> {

    void receiveCoupon(Long id);

    void exchangeCoupon(String code);

//    public void checkAndCreateUserCoupon(Long userId, Coupon coupon, Long serialNum);

    PageDTO<CouponVO> queryMyCouponPage(UserCouponQuery query);

    void checkAndCreateUserCouponNew(UserCouponDTO msg);


    List<CouponDiscountDTO> findDiscountSolution(List<OrderCourseDTO> dto);

    CouponDiscountDTO calculateSolutionDiscount(Map<Coupon, List<OrderCourseDTO>> availableCouponMap, List<OrderCourseDTO> courseList, List<Coupon> coupons);

    //细筛，查询每个优惠券对应的可用课程
    Map<Coupon, List<OrderCourseDTO>> findAvailableCoupons(List<Coupon> coupons, List<OrderCourseDTO> orderCourses);

    void writeOffCoupon(List<Long> userCouponIds);

    void refundCoupon(List<Long> userCouponIds);

    List<String> queryDiscountRules(List<Long> userCouponIds);

    CouponDiscountDTO queryDiscountDetailByOrder(OrderCouponDTO orderCouponDTO);

    List<Long> transformCouponIds(List<Long> couponIds);
}
