package com.yunfan.learning.service;

import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.common.domain.query.PageQuery;
import com.yunfan.learning.domain.dto.LearningPlanDTO;
import com.yunfan.learning.domain.po.LearningLesson;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.learning.domain.vo.LearningLessonVO;
import com.yunfan.learning.domain.vo.LearningPlanPageVO;

import java.util.List;

/**
 * <p>
 * 学生课程表 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2023-10-22
 */
public interface ILearningLessonService extends IService<LearningLesson> {

    void addUserLesson(Long userId, List<Long> courseIds);

    PageDTO<LearningLessonVO> queryMyLessons(PageQuery query);

    LearningLessonVO queryMyCurrentLesson();

    LearningLessonVO queryLessonByCourseId(Long courseId);

    Long isLessonValid(Long courseId);

    Integer countLearningLessonByCourse(Long courseId);

    void createLearningPlan(LearningPlanDTO dto);

    LearningPlanPageVO queryMyPlans(PageQuery query);

    void deleteMyLessons(Long id);
}
