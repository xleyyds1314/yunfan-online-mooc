package com.yunfan.exam.domain.vo;

import com.yunfan.api.dto.exam.QuestionDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "考试题目vo")
public class ExamQuestionVO {
    private Long id;
    private List<QuestionDTO> questions;
}
