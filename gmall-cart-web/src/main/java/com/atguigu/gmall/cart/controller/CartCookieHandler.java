package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.CookieUtil;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import javax.persistence.Transient;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作cookie的类
 * @author wangyanjie
 * @create 2018-09-05 - 19:44
 */
@Component
public class CartCookieHandler {

    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;

    @Reference
    private ManageService manageService;

    /**
     * 未登录时添加
     * @param request
     * @param response
     * @param skuId
     * @param userId
     * @param skuNum
     */
    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, Integer skuNum){
        //判断当前购物车中是否存在
        String cartJson = CookieUtil.getCookieValue(request, cookieCartName, true);
        boolean ifExist = false;
        List<CartInfo> cartInfoList = new ArrayList<>();
        //判断
        if(cartJson!=null && cartJson.length()>0){

            //将字符串转为对象
            cartInfoList = JSON.parseArray(cartJson, CartInfo.class);

            //存在,购物车中有商品
            for (CartInfo cartInfo : cartInfoList) {
                if(cartInfo.getSkuId().equals(skuId)){
                    //购物车数量相加
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                    //重新放入cookie中
                    ifExist=true;
                }
            }
        }

        //购物车中该商品不存在，要添加到购物车中
        if(!ifExist){
            //把商品取出来，新增到购物车
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo = new CartInfo();

//            String userId;
//            String skuId;
//            BigDecimal cartPrice;
//            Integer skuNum;
//            String imgUrl;
//            String skuName;
//            BigDecimal skuPrice;
//            String isChecked="0";
            cartInfo.setUserId(userId);
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuNum(skuNum);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setSkuPrice(skuInfo.getPrice());

            //保存
            cartInfoList.add(cartInfo);
        }
        //将数据重新放入到cookie中
        CookieUtil.setCookie(request,response,cookieCartName,JSON.toJSONString(cartInfoList),COOKIE_CART_MAXAGE,true);
    }

    public List<CartInfo> getCartList(HttpServletRequest request) {
        //直接从cookie中取值
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);
        //判断
        if(cookieValue!=null && cookieValue.length()>0){
            //cookie中有值，将字符串转为集合对象
            List<CartInfo> cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
            //直接返回
            return cartInfoList;
        }
        return null;
    }

    /**
     * 删除cookie中的数据
     * @param request
     * @param response
     */
    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request,response,cookieCartName);
    }
}
