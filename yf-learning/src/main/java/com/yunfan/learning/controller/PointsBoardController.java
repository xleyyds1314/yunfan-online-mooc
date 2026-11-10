package com.yunfan.learning.controller;

import com.yunfan.common.utils.BeanUtils;
import com.yunfan.common.utils.CollUtils;
import com.yunfan.learning.domain.po.PointsBoardSeason;
import com.yunfan.learning.domain.query.PointsBoardQuery;
import com.yunfan.learning.domain.vo.PointsBoardVO;
import com.yunfan.learning.service.IPointsBoardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 学霸天梯榜 控制器
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/boards")
@Api(tags = "排行榜相关接口")
public class PointsBoardController {

    private final IPointsBoardService pointsBoardService;

    @GetMapping
    @ApiOperation("学霸排行榜-分页查询指定赛季的积分排行榜")
    public PointsBoardVO queryPointsBoardList(PointsBoardQuery query){
        return pointsBoardService.queryPointsBoardList(query);
    }

}