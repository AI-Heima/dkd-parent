package com.dkd.manage.domain.vo;

import lombok.Data;

@Data
public class SkuVo {

    /** 商品名称 */
    private String skuName;

    /** 商品销量 */
    private int saleNum;
}
