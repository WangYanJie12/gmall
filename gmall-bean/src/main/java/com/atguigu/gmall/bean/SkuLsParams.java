package com.atguigu.gmall.bean;

import java.io.Serializable;

/**
 * @author wangyanjie
 * @create 2018-09-02 - 13:20
 */
public class SkuLsParams implements Serializable {

    /**
     * 关键字查询
     */
    String  keyword;

    /**
     * catalog3Id查询
     */
    String catalog3Id;

    String[] valueId;

    int pageNo=1;

    int pageSize=20;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(String catalog3Id) {
        this.catalog3Id = catalog3Id;
    }

    public String[] getValueId() {
        return valueId;
    }

    public void setValueId(String[] valueId) {
        this.valueId = valueId;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
