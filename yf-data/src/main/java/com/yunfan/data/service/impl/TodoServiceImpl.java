package com.yunfan.data.service.impl;

import com.yunfan.api.dto.data.TodoDataVO;
import com.yunfan.common.utils.BeanUtils;
import com.yunfan.common.utils.JsonUtils;
import com.yunfan.data.constants.RedisConstants;
import com.yunfan.data.service.TodoService;
import com.yunfan.data.utils.DataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @ClassName TodoListServiceImpl
 * @author xleyyds1314
 * @since 2025-5-18 19:06:26
 **/
@Service
public class TodoServiceImpl implements TodoService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public TodoDataVO get() {
        // 1.数据redis存储key
        String key = RedisConstants.KEY_TODO + DataUtils.getVersion(1);
        // 2.获取数据
        Object originData = redisTemplate.opsForValue().get(key);
        // 2.1.数据判空
        if (originData == null) {
            return new TodoDataVO();
        }
        return JsonUtils.toBean(originData.toString(), TodoDataVO.class);
    }

    @Override
    public void updateTodoData(TodoDataVO vo) {
        if(vo==null){
            return;
        }
        TodoDataVO todoDataVO = get();
        if(vo.getTodoCouponNum()!=null){
            todoDataVO.setTodoCouponNum(vo.getTodoCouponNum());
        }else if(vo.getTodoRefundNum()!=null){
            todoDataVO.setTodoRefundNum(vo.getTodoRefundNum());
        }
        redisTemplate.opsForValue().set(RedisConstants.KEY_TODO + DataUtils.getVersion(1)
                , JsonUtils.toJsonStr(todoDataVO));

    }
}
