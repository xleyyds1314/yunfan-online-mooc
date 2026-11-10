package com.yunfan.message.domain.query;/**
 * @author xleyyds1314
 * @date 2025/5/28 9:13
 */

import com.yunfan.common.domain.query.PageQuery;
import lombok.Data;

/**
 * @Author: fsq
 * @Date: 2025/5/28 9:13
 * @Version: 1.0
 */
@Data
public class ChatHistoryQuery extends PageQuery {
    private Long id;//群聊id  或用户id
    private Integer type;

}
