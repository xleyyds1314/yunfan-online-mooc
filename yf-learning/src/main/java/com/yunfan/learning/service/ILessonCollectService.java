package com.yunfan.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.learning.domain.dto.CollectFormDTO;
import com.yunfan.learning.domain.po.LessonCollect;
import com.yunfan.learning.domain.query.LessonCollectQuery;
import com.yunfan.learning.domain.vo.LessonCollectVO;

/**
 * @author xleyyds1314
 * @date 2025/5/22 9:04
 */
public interface ILessonCollectService extends IService<LessonCollect> {

    PageDTO<LessonCollectVO> queryMyCollects(LessonCollectQuery query);

    void addCollect(CollectFormDTO dto);

    Boolean isCollected(Long lessonId);
}
