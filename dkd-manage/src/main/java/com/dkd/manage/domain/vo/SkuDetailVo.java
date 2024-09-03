package com.dkd.manage.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class SkuDetailVo {

    // 销售额
    private Integer salesAmount;

    // 补货次数
    private Integer replenishCount;

    // 维修次数
    private Integer repairCount;

    // 商品销售列表
    private List<SkuVo> skuList;
}
