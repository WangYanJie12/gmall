package com.atguigu.gmall.manager.mapper;

import com.atguigu.gmall.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author wangyanjie
 * @create 2018-08-27 - 16:48
 */
public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {

    List<SpuSaleAttr> selectSpuSaleAttrList(String spuId);

    /**
     * 查询spu销售属性名的集合
     * @param spuId
     * @param id
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(long id,long spuId);
}
