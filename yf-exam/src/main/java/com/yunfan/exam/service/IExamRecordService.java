package com.yunfan.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.common.domain.query.PageQuery;
import com.yunfan.exam.domain.dto.ExamCommitDTO;
import com.yunfan.exam.domain.dto.ExamFormDTO;
import com.yunfan.exam.domain.po.ExamRecord;
import com.yunfan.exam.domain.query.ExamPageQuery;
import com.yunfan.exam.domain.vo.ExamQuestionVO;
import com.yunfan.exam.domain.vo.ExamRecordAdminVO;
import com.yunfan.exam.domain.vo.ExamRecordDetailVO;
import com.yunfan.exam.domain.vo.ExamRecordVO;

import java.util.List;

/**
 * <p>
 * 考试记录表 服务类
 * </p>
 *
 * @author xleyyds1314
 */
public interface IExamRecordService extends IService<ExamRecord> {

    PageDTO<ExamRecordVO> queryMyExamRecordsPage(PageQuery query);

    ExamQuestionVO saveExamRecord(ExamFormDTO examFormDTO);

    void saveExamRecordDetails(ExamCommitDTO examCommitDTO);

    List<ExamRecordDetailVO> queryDetailsByExamId(Long examId);

    PageDTO<ExamRecordAdminVO> queryAdminExamRecordsPage(ExamPageQuery query);

    List<ExamRecordDetailVO> queryAdminDetailsByExamId(Long examId);

}
