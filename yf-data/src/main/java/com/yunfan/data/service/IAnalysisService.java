package com.yunfan.data.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.common.domain.query.PageQuery;
import com.yunfan.data.influxdb.domain.BusinessLog;
import com.yunfan.data.model.po.Dnu;
import com.yunfan.data.model.po.LogAnalysisResult;
import com.yunfan.data.model.po.UserProfile;
import com.yunfan.data.model.query.FlowQuery;
import com.yunfan.data.model.vo.CourseProfileVO;
import com.yunfan.data.model.vo.EchartsVO;
import com.yunfan.data.model.vo.FunnelPlotChartsVO;
import com.yunfan.data.model.vo.UserProfileVO;

import java.util.List;

/**
 * @Description：数据分析服务类接口
 */
public interface IAnalysisService {

    FunnelPlotChartsVO courseConversionDpv(FlowQuery query);

    EchartsVO courseDetailGenderDuv(FlowQuery query);

    EchartsVO courseDetailProvinceDuv(FlowQuery query);

    LogAnalysisResult analyzeLogs(List<BusinessLog> logs);

    PageDTO<UserProfileVO> getAnalysisResultByUser(PageQuery query);

    PageDTO<CourseProfileVO> getAnalysisResultByCourse(PageQuery query);
}
