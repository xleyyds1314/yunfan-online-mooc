package com.yunfan.data.service;

import com.yunfan.common.domain.R;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.data.influxdb.domain.BusinessLog;
import com.yunfan.data.model.query.UrlPageQuery;
import com.yunfan.data.model.query.UrlQuery;
import com.yunfan.data.model.vo.EchartsVO;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @ClassName IBusinessLogService.java
 * @Description 日志服务接口
 */
public interface IUrlLogService {

    /**
     * 分析指定url的访问数据
     * @param query
     * @return
     */
    PageDTO<BusinessLog> getLogsPageByUrl(UrlPageQuery  query);

    /**
     * 分析模糊url的访问数据
     * @param query
     * @return
     */
    PageDTO<BusinessLog> getLogsPageByUrlByLike(UrlPageQuery query);

    /**
     * 获取指定url的指标数据
     * @param query
     * @return
     */
    EchartsVO getMetricByUrl(UrlQuery query);

    /**
     * 获取模糊url的指标数据
     * @param query
     * @return
     */
    EchartsVO getMetricByUrlByLike(UrlQuery query);

    /**
     * 导出全部日志数据
     * @return
     */
    void exportLogs(HttpServletResponse response) throws IOException;
}
