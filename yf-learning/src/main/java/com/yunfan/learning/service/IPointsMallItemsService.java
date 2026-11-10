package com.yunfan.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.common.domain.query.PageQuery;
import com.yunfan.learning.domain.po.PointsMallItems;
import com.yunfan.learning.domain.query.PointsItemsPageQuery;

public interface IPointsMallItemsService extends IService<PointsMallItems> {

    PageDTO<PointsMallItems> queryPointsItemsByPage(PointsItemsPageQuery query);
}