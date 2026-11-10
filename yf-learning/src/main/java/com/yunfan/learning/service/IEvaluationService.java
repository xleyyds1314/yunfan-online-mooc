package com.yunfan.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.api.dto.leanring.EvaluationScoreDTO;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.learning.domain.dto.EvaluationDTO;
import com.yunfan.learning.domain.po.Evaluation;
import com.yunfan.learning.domain.po.InteractionQuestion;
import com.yunfan.learning.domain.query.EvaluationQuery;
import com.yunfan.learning.domain.vo.EvaluationDetailVO;
import com.yunfan.learning.domain.vo.EvaluationVO;

import java.util.List;

/**
 * @author xleyyds1314
 * @date 2025/5/22 11:39
 */
public interface IEvaluationService extends IService<Evaluation> {

    PageDTO<EvaluationVO> queryEvaluationPage(EvaluationQuery query);

    void saveEvaluation(EvaluationDTO dto);

    EvaluationDTO queryEvaluationById(Long id);

    Boolean updateEvaluation(EvaluationDTO dto);

    Boolean deleteEvaluation(Long id);

    Boolean isEvaluated(Long courseId);

    EvaluationDetailVO queryEvaluationDetailById(Long id);

    void  getAllCourseAvgScore();
}
