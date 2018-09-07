package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List; /**
 * 商品购物车相关接口
 * @author wangyanjie
 * @create 2018-09-05 - 18:13
 */
public interface CartService {

    /**
     * 添加商品到购物车
     * @param skuId
     * @param userId
     * @param skuNum
     */
    void addToCart(String skuId,String userId,Integer skuNum);

    /**
     * 将redis和cookie中的数据进行合并
     * @param cartListCK
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartListCK, String userId);

    /**
     * 从redis中获取
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);

    /**
     * 缓存中没有数据，则从 数据库中加载
     * @param userId
     * @return
     */
    List<CartInfo> loadCartCache(String userId);

}
