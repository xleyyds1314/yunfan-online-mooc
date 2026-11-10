package com.yunfan.learning.service;

import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.learning.domain.dto.ReplyDTO;
import com.yunfan.learning.domain.po.InteractionReply;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.learning.domain.query.ReplyPageQuery;
import com.yunfan.learning.domain.vo.QuestionVO;
import com.yunfan.learning.domain.vo.ReplyVO;

/**
 * <p>
 * 互动问题的回答或评论 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2023-10-23
 */
public interface IInteractionReplyService extends IService<InteractionReply> {

    void saveReply(ReplyDTO dto);

    void hiddenReply(Long id, Boolean hidden);

    PageDTO<ReplyVO> queryReplyPage(ReplyPageQuery pageQuery, boolean forAdmin);

    ReplyVO queryReplyById(Long id);
}
