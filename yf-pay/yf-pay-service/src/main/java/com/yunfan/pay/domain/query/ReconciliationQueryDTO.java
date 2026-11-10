package com.yunfan.pay.domain.query;

import com.yunfan.common.domain.query.PageQuery;
import lombok.Data;

@Data
public class ReconciliationQueryDTO extends PageQuery {
    private Integer status; // 可选，对账状态
}