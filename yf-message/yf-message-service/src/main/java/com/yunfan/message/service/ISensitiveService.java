package com.yunfan.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfan.common.domain.dto.PageDTO;
import com.yunfan.message.domain.po.Sensitive;
import com.yunfan.message.domain.query.SensitiveQuery;

import java.util.List;

public interface ISensitiveService extends IService<Sensitive> {
    PageDTO<Sensitive> getAllSensitiveWords(SensitiveQuery query);

    boolean saveSensitive(Sensitive sensitive);

    boolean updateSensitive(Sensitive sensitive);

    boolean deleteSensitive(Long id);
}
