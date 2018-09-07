package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;

/**
 * @author wangyanjie
 * @create 2018-09-01 - 17:01
 */
public interface ListService {

    /**
     * 根据skuLsInfo保存数据，对es数据封装的对象
     * @param skuLsInfo
     */
    void saveSkuInfo(SkuLsInfo skuLsInfo);

    /**
     * 根据用户输入的信息，返回封装数据的结果集
     * @param skuLsParams
     * @return
     */
    SkuLsResult search(SkuLsParams skuLsParams);

    /**
     * 根据热度进行排名，为当前的商品增加热度
     * @param skuId
     */
    void incrHotScore(String skuId);
}
