package com.yunfan.learning.service;

import com.yunfan.learning.domain.vo.SignResultVO;

public interface ISignRecordsService {
    SignResultVO addSignRecords();

    Byte[] querySignRecords();
}
