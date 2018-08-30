package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manager.mapper.*;
import com.atguigu.gmall.service.ManageService;
import javafx.scene.image.Image;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.text.html.HTMLDocument;
import java.util.List;

/**
 * @author wangyanjie
 * @create 2018-08-24 - 20:12
 */
@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        //BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        //baseAttrInfo.setCatalog3Id(catalog3Id);
        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(Long.parseLong(catalog3Id));
        //return baseAttrInfoMapper.select(baseAttrInfo);
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //判断当前的id是否存在
        if(baseAttrInfo.getId()!=null && baseAttrInfo.getId().length()>0){
            //当前id存在，此时是要进行的是更新操作
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        }else{
            //防止主键被覆上空串
            if(baseAttrInfo.getId().length()==0){
                baseAttrInfo.setId(null);
            }
            //当前id不存在，是插入操作
            baseAttrInfoMapper.insert(baseAttrInfo);
        }
        //插入平台属性值，需要把原来的属性值删除
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue);

        //重新插入属性值
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue attrValue : attrValueList) {
            if(attrValue.getId()!=null && attrValue.getId().length()>0){
                // 更新
                baseAttrValueMapper.updateByPrimaryKey(attrValue);
            }else{
                //防止主键被覆上空串
                if(attrValue.getId().length()==0){
                    attrValue.setId(null);
                }
                //插入
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }

    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        // 创建属性对象
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        // 创建属性值对象
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        // 根据attrId字段查询对象
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
        // 给属性对象中的属性值集合赋值
        baseAttrInfo.setAttrValueList(baseAttrValueList);
        // 将属性对象返回
        return baseAttrInfo;

    }

    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //判断当前id是否存在
        if(spuInfo.getId()==null || spuInfo.getId().length()==0){
            //id不存在的时候是插入数据，将id设置为null，防止主键被设置为空串
            spuInfo.setId(null);
            //插入数据
            spuInfoMapper.insertSelective(spuInfo);
        }else{
            //id存在时是更新数据
            spuInfoMapper.updateByPrimaryKeySelective(spuInfo);
        }

        //插入spuImg之前，根据spuId将原来的数据删除
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuInfo.getId());
        spuImageMapper.delete(spuImage);

        //插入图片数据
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(spuImageList!=null&&spuImageList.size()>0){
            for (SpuImage image : spuImageList) {
                if(image.getId()!=null&&image.getId().length()==0){
                //id不存在的时候将id置为null
                    image.setId(null);
                }
                //id存在的时候，设置id
                image.setSpuId(spuInfo.getId());
                //插入数据的操作
                spuImageMapper.insertSelective(image);
            }
        }else{
            //更新数据的操作
            spuImageMapper.updateByPrimaryKeySelective(spuImage);
        }


        //保存销售属性和销售属性值的操作，保存之前先将原来的数据删除
        //删除原来的属性名
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);

        //删除原来的属性值
        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);

        //插入销售属性名
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if(spuSaleAttrList!=null&&spuSaleAttrList.size()>0){
            for (SpuSaleAttr saleAttr : spuSaleAttrList) {
                if(saleAttr.getId()!=null&&saleAttr.getId().length()==0){
                    saleAttr.setId(null);
                }
                saleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(saleAttr);

                //插入属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
                for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                    if(saleAttrValue.getId()!=null&&saleAttrValue.getId().length()==0){
                        saleAttrValue.setId(null);
                    }
                    saleAttrValue.setSpuId(spuInfo.getId());
                    spuSaleAttrValueMapper.insertSelective(saleAttrValue);
                }
            }

        }

    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    public void saveSku(SkuInfo skuInfo) {
        //1、插入spuInfo，先进性判断
        if(skuInfo.getId()==null || skuInfo.getId().length()==0){
            //id不存在是插入数据，将id设置为null，防止主键被赋值为空串
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        }else{
            //id存在，是更新数据
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }

        //2、插入图片属性skuImage
        //先删除原先的图片
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImage);
        
        //删除完成之后，再插入skuImage
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(skuImageList != null && skuImageList.size()>0){
            for (SkuImage image : skuImageList) {
                if(image.getId()!=null && image.getId().length()==0){
                    image.setId(null);
                }
                image.setSkuId(skuInfo.getId());
                //插入
                skuImageMapper.insertSelective(image);
            }
        }else{
            //更新数据
            skuImageMapper.updateByPrimaryKeySelective(skuImage);
        }

        //3、sku_sale_attr_value

        //删除原来已经存在的数据
        SkuSaleAttrValue skuSaleAttrValue1 = new SkuSaleAttrValue();
        skuSaleAttrValue1.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue1);

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if(skuSaleAttrValueList != null && skuSaleAttrValueList.size()>0){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                if(skuSaleAttrValue.getId()!=null && skuSaleAttrValue.getId().length()==0){
                    skuSaleAttrValue.setId(null);
                }
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                //插入数据
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }else{
            //更新数据
            skuSaleAttrValueMapper.updateByPrimaryKeySelective(skuSaleAttrValue1);
        }



        //4、sku_attr_value

        //删除之前存在的数据
        SkuAttrValue skuAttrValue1 = new SkuAttrValue();
        skuAttrValue1.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue1);

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if(skuAttrValueList!=null && skuAttrValueList.size()>0){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                if(skuAttrValue.getId()!=null && skuAttrValue.getId().length()==0){
                    skuAttrValue.setId(null);
                }
                skuAttrValue.setSkuId(skuInfo.getId());
                //插入数据
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }else{
            //更新数据
            skuAttrValueMapper.updateByPrimaryKeySelective(skuAttrValue1);
        }
    }

    @Override
    public List<SkuInfo> getSkuInfoListBySpu(String spuId) {
        return skuInfoMapper.selectSkuInfoListBySpu(Long.parseLong(spuId));
    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);
        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(Long.parseLong(skuInfo.getId()),Long.parseLong(skuInfo.getSpuId()));
    }
}
