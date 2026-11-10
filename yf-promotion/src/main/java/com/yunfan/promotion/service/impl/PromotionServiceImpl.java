package com.yunfan.promotion.service.impl;

import com.yunfan.promotion.domain.po.Promotion;
import com.yunfan.promotion.mapper.PromotionMapper;
import com.yunfan.promotion.service.IPromotionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 促销活动，形式多种多样，例如：优惠券 服务实现类
 * </p>
 *
 * @author xleyyds1314
 * @since 2023-10-28
 */
@Service
public class PromotionServiceImpl extends ServiceImpl<PromotionMapper, Promotion> implements IPromotionService {

}
