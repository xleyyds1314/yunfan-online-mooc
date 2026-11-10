package com.yunfan.exam.mapper;

import com.yunfan.api.dto.IdAndNumDTO;
import com.yunfan.exam.domain.po.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 题目 Mapper 接口
 * </p>
 *
 * @author xleyyds1314
 * @since 2022-09-02
 */
public interface QuestionMapper extends BaseMapper<Question> {

    List<IdAndNumDTO> countQuestionOfCreater(@Param("createrIds") List<Long> createrIds);

}
