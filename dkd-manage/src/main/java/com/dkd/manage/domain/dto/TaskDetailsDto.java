package com.dkd.manage.domain.dto;

import lombok.Data;

@Data
public class TaskDetailsDto {

    private String channelCode; // 货道编号

    private Number expectCapacity; // 补货数量

    private Number skuId; // 商品Id

    private String skuName; // 商品名称

    private String skuImage; // 商品图片
}
