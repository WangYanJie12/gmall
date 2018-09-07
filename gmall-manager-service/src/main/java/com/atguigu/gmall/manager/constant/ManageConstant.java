package com.atguigu.gmall.manager.constant;

/**
 * @author wangyanjie
 * @create 2018-08-31 - 9:01
 */
public class ManageConstant {

    public static final String SKUKEY_PREFIX="sku:";

    public static final String SKUKEY_SUFFIX=":info";

    public static final int SKUKEY_TIMEOUT=24*60*60;

    //解决缓存击穿问题
    public static final int SKULOCK_EXPIRE_PX=10000;

    public static final String SKULOCK_SUFFIX=":lock";


}
