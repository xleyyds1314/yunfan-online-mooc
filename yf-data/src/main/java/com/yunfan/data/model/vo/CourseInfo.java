package com.yunfan.data.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName CourseInfo
 * @Author xleyyds1314
 * @Date 2022/10/10 19:33
 * @Version
 **/
@Data
public class CourseInfo implements Serializable {
    private String category;
    private String name;
    private Integer newStuNum;
    private Double orderAmount;
}
