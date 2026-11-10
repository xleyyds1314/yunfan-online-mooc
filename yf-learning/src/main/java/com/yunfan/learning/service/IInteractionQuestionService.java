package com.yunfan.learning.service;

import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.learning.domain.dto.QuestionFormDTO;
import com.yunfan.learning.domain.po.InteractionQuestion;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.learning.domain.query.QuestionAdminPageQuery;
import com.yunfan.learning.domain.query.QuestionPageQuery;
import com.yunfan.learning.domain.vo.QuestionAdminVO;
import com.yunfan.learning.domain.vo.QuestionVO;

/**
 * <p>
 * 互动提问的问题表 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2023-10-23
 */
public interface IInteractionQuestionService extends IService<InteractionQuestion> {

    void saveQuestion(QuestionFormDTO dto);

    void updateQuestion(Long id, QuestionFormDTO dto);

    PageDTO<QuestionVO> queryQuestionPage(QuestionPageQuery query);

    QuestionVO queryQuestionById(Long id);

    PageDTO<QuestionAdminVO> queryQuestionAdminVOPage(QuestionAdminPageQuery query);

    void deleteQuestion(Long id);

    void hiddenQuestion(Long id, boolean hidden);

    QuestionAdminVO queryQuestionAdminById(Long id);
}
