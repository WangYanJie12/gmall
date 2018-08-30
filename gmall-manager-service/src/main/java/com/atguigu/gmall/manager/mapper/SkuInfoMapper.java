package com.atguigu.gmall.manager.mapper;

import com.atguigu.gmall.bean.SkuInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author wangyanjie
 * @create 2018-08-28 - 8:28
 */
public interface SkuInfoMapper extends Mapper<SkuInfo> {
    /**
     * 根据spuId查询sku列表
     * @param spuId
     * @return
     */
    List<SkuInfo> selectSkuInfoListBySpu(long spuId);
}
