package com.yunfan.exam.controller;

import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.common.domain.query.PageQuery;
import com.yunfan.exam.domain.dto.ExamCommitDTO;
import com.yunfan.exam.domain.dto.ExamDetailCommentDTO;
import com.yunfan.exam.domain.dto.ExamFormDTO;
import com.yunfan.exam.domain.query.ExamPageQuery;
import com.yunfan.exam.domain.vo.ExamQuestionVO;
import com.yunfan.exam.domain.vo.ExamRecordAdminVO;
import com.yunfan.exam.domain.vo.ExamRecordDetailVO;
import com.yunfan.exam.domain.vo.ExamRecordVO;
import com.yunfan.exam.service.IExamRecordDetailService;
import com.yunfan.exam.service.IExamRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 考试记录表 控制器
 * </p>
 *
 * @author xleyyds1314
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "管理端查询考试记录相关接口")
@RequestMapping("/admin/exams")
public class ExamRecordAdminController {

    private final IExamRecordService examService;
    private final IExamRecordDetailService detailService;


    @GetMapping("/page")
    @ApiOperation("分页查询考试记录")
    public PageDTO<ExamRecordAdminVO> queryExamRecords(ExamPageQuery query){
        return examService.queryAdminExamRecordsPage(query);
    }

    @ApiOperation("查询考试记录详情")
    @GetMapping("/{examId}")
    public List<ExamRecordDetailVO> queryDetailsByExamId(
            @ApiParam(value = "考试记录id", example = "1") @PathVariable("examId") Long examId
    ){
        return examService.queryAdminDetailsByExamId(examId);
    }

    @ApiOperation("教师评语")
    @PostMapping("/comment")
    public void addComment(@RequestBody List<ExamDetailCommentDTO> dtos){
        detailService.addComment(dtos);
    }


}
