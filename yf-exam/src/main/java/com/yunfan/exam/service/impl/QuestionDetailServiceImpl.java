package com.yunfan.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunfan.exam.domain.po.QuestionDetail;
import com.yunfan.exam.mapper.QuestionDetailMapper;
import com.yunfan.exam.service.IQuestionDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 题目 服务实现类
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-09-02
 */
@Service
public class QuestionDetailServiceImpl extends ServiceImpl<QuestionDetailMapper, QuestionDetail> implements IQuestionDetailService {

}
