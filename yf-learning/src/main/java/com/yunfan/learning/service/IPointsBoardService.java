package com.yunfan.learning.service;

import com.yunfan.learning.domain.po.PointsBoard;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.learning.domain.query.PointsBoardQuery;
import com.yunfan.learning.domain.vo.PointsBoardVO;

import java.util.List;

/**
 * <p>
 * 学霸天梯榜 服务类
 * </p>
 *
 * @author xleyyds1314
 * @since 2023-10-26
 */
public interface IPointsBoardService extends IService<PointsBoard> {


    PointsBoardVO queryPointsBoardList(PointsBoardQuery query);

    List<PointsBoard> queryCurrentBoard(String key, Integer pageNo, Integer pageSize);


}
