package com.yunfan.data.service;


import com.yunfan.api.dto.data.TodoDataVO;
import com.yunfan.data.model.dto.Top10DataSetDTO;
import com.yunfan.data.model.vo.Top10DataVO;

/**
 * @author xleyyds1314
 * @since 2022/10/10 19:39
 **/
public interface TodoService {

    /**
     * 待办事项
     * @return
     */
    TodoDataVO get();

    /**
     * 更新待办事项
     * @param vo
     */
    void updateTodoData(TodoDataVO vo);
}