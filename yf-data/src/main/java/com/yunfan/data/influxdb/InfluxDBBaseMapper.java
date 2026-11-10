package com.yunfan.data.influxdb;

import com.yunfan.data.influxdb.anno.Insert;

import java.util.List;

public interface InfluxDBBaseMapper<T> {

    @Insert
    void insertOne(T entity);

    @Insert
    void insertBatch(List<T> entityList);
}
