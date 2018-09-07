package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

/**
 * @author wangyanjie
 * @create 2018-08-24 - 20:04
 */
public interface ManageService {

    /**
     * 查询所有一级菜单
     * @return
     */
    List<BaseCatalog1> getCatalog1();

    /**
     * 根据一级菜单的id查询所有二级菜单
     * @param catalog1Id
     * @return
     */
    List<BaseCatalog2> getCatalog2(String catalog1Id);

    /**
     * 根据二级菜单的id查询所有三级菜单
     * @param catalog2Id
     * @return
     */
    List<BaseCatalog3> getCatalog3(String catalog2Id);

    /**
     * 根据三级菜单的id查询平台属性列表
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> getAttrList(String catalog3Id);

    /**
     * 保存平台属性，平台属性值
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 编辑属性值
     * @param attrId
     * @return
     */
    BaseAttrInfo getAttrInfo(String attrId);

    /**
     * 根据三级菜单的id查询平台属性列表
     * @param spuInfo
     * @return
     */
    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    /**
     * 获得销售属性值
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存spu所有数据
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 获得商品图片列表
     * @param spuId
     * @return
     */
    List<SpuImage> getSpuImageList(String spuId);

    /**
     * 获取销售属性值
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    /**
     * 大保存，保存平台属性，图片，和描述
     * @param skuInfo
     */
    void saveSku(SkuInfo skuInfo);

    /**
     * 加载skuInfoList列表
     * @param spuId
     * @return
     */
    List<SkuInfo> getSkuInfoListBySpu(String spuId);

    /**
     * 根据skuId获取sku的销售属性值和描述，图片。并且存到request域中，要在页面中动态获取
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(String skuId);

    /**
     * 根据skuInfo对象查询spu销售属性名，保存到域中
     * @param skuInfo
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    /**
     * 根据spuId查询所有sku销售属性值的列表
     * @param spuId
     * @return
     */
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);

    /**
     * 根据平台属性值的id，查询到平台属性名，平台属性值
     * @param attrValueIdList
     * @return
     */
    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
