package com.yunfan.data.service;

import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.data.influxdb.domain.BusinessLog;
import com.yunfan.data.model.query.FlowQuery;
import com.yunfan.data.model.query.UrlPageQuery;
import com.yunfan.data.model.query.UrlQuery;
import com.yunfan.data.model.vo.EchartsVO;

/**
 * @ClassName IFlowService.java
 * @Description 流量统计服务接口
 */
public interface IFlowService {

    EchartsVO dnu(FlowQuery query);
    EchartsVO dpv(FlowQuery query);
    EchartsVO duv(FlowQuery query);
    EchartsVO dau(FlowQuery query);

    EchartsVO dpvTime(FlowQuery query);

    EchartsVO dauProvince(FlowQuery query);
}
