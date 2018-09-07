package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author wangyanjie
 * @create 2018-09-02 - 17:18
 */
@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;


    @RequestMapping("list.html")
    public String list(SkuLsParams skuLsParams, HttpServletRequest request){

        //设置分页
        skuLsParams.setPageSize(3);
        //将分页数据传入到结果集中
        SkuLsResult skuLsResult = listService.search(skuLsParams);
        //将对象转为字符串
        String s = JSON.toJSONString(skuLsResult);
        System.out.println("s="+s);

        //从es中取得平台属性值的id集合
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        //根据平台属性值的id，查询到平台属性名，平台属性值
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);

        //已选的属性值列表，制作要往地址栏拼接的url路径
        String urlParam = makeUrlParam(skuLsParams);
        
        //面包屑功能，先声明一个集合
        ArrayList<BaseAttrValue> baseAttrValueList = new ArrayList<>();

        // 过滤重复属性值 循环attrList --  skuLsParams.getValueId() 页面得到的valueId 比较结果如何相同，则将数据进行remove？
        // 集合-- 能否在遍历的过程中进行删除集合中的数据？(用迭代器循环，itco)
        for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
            //取得平台属性对象
            BaseAttrInfo baseAttrInfo =  iterator.next();
            //获取平台属性值的集合
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            //进行循环比较
            for (BaseAttrValue baseAttrValue : attrValueList) {
                //取得平台属性值对象
                baseAttrValue.setUrlParam(urlParam);
                //获取url中的每一个平台属性id值，即valueId的值，如果和点击的valueId值相同，为避免重复，将其进行删除
                if(skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
                    //取得url路径中的每一个valueId的值
                    for (String valueId : skuLsParams.getValueId()) {
                        //判断平台属性id值和点点击输入的valueId值是否相同，
                        if(valueId.equals(baseAttrValue.getId())){
                            //如果相同，将valueId移除
                            iterator.remove();

                            //面包屑功能，取出属性名和属性值，添加到集合中，在页面循环展现
                            BaseAttrValue baseAttrValueSelected = new BaseAttrValue();
                            //添加到面包屑页面上展现的方式是(属性名：属性值)
                            baseAttrValueSelected.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
                            //去重复操作
                            String makeUrlParam = makeUrlParam(skuLsParams, valueId);
                            //面包屑中的url赋值
                            baseAttrValueSelected.setUrlParam(makeUrlParam);
                            baseAttrValueList.add(baseAttrValueSelected);
                        }
                    }
                }

            }

        }

        //分页
        request.setAttribute("totalPages",skuLsResult.getTotalPages());
        request.setAttribute("pageNo",skuLsParams.getPageNo());

        //将面包屑保存，在页面上展示
        request.setAttribute("baseAttrValueList",baseAttrValueList);

        //将要拼接的url路径进行保存，在页面获取
        request.setAttribute("urlParam",urlParam);

        //将keyword关键字进行保存，在页面获取
        request.setAttribute("keyword",skuLsParams.getKeyword());

        //将skuLsInfo列表进行保存
        request.setAttribute("skuLsInfoList",skuLsResult.getSkuLsInfoList());

        //将平台属性值和平台属性名的列表进行保存
        request.setAttribute("attrList",attrList);
        return "list";
    }


    /**
     * 制作要往地址栏拼接的url路径
     * @param skuLsParams
     * @return
     */
    private String makeUrlParam(SkuLsParams skuLsParams,String ... excludeValueIds) {
//        http://list.gmall.com/list.html?keyword=小米&catalog3Id=61&valueId=13&pageNo=1&pageSize=10
//        参数传递 一个参数，多个参数：
//        一个参数：连接后面加？
//        多个参数：第一个是？第二个后续都是&连接。

        String urlParam = "";

        //关键字keyword
        if(skuLsParams.getKeyword()!=null && skuLsParams.getKeyword().length()>0){
            urlParam += "keyword="+skuLsParams.getKeyword();
        }

        //catalog3Id
        if(skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            if(urlParam.length()>0){
                urlParam += "&";
            }
            urlParam += "catalog3Id="+skuLsParams.getCatalog3Id();
        }

        //构造属性参数 valueId
        if(skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){
            //循环拼接匹配
            for (String valueId : skuLsParams.getValueId()) {
                if(excludeValueIds!=null && excludeValueIds.length>0){
                    //因为每次点击的时候，只能点击一个过滤属性，所以这里是从0开始
                    String excludeValueId = excludeValueIds[0];
                    if(valueId.equals(excludeValueId)){
                        continue;
                    }
                }
                if(urlParam.length()>0){
                    urlParam += "&";
                }
                urlParam += "valueId="+valueId;
            }
        }

        return urlParam;
    }
}
