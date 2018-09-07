package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 购物车控制器
 * @author wangyanjie
 * @create 2018-09-05 - 19:31
 */
@Controller
public class CartController {

    @Reference
    private CartService cartService;

    //操作cookie的类
    @Autowired
    private CartCookieHandler cartCookieHandler;

    @Reference
    private ManageService manageService;


    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        //获取参数，skuNum，skuId，userId
        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");
        //userId之前我们存在了拦截器中，从拦截器中可以获取
        String userId = (String) request.getAttribute("userId");

        //判断当前用户是否存在(登录或者没登录)
        if(userId!=null){
            //当前已经登录，将数据保存在redis中
            cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else{
            //当前用户没有登录，数据保存在cookie中
            cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
        }
        //添加成功页面需要skuInfo对象
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        //将对象存到域对象中，在页面获取属性值
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        //返回添加成功页面
        return "success";
    }

    /**
     * 去购物车结算
     */
    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response){
        //展示分为登录展示和未登录展示，如果登录了，从redis中获取，redis中没有，就从数据库获取
        //没有登录，从cookie中获取
        String userId = (String) request.getAttribute("userId");
        //判断用户是否登录
        if(userId!=null){
            //用户已经登录，从redis中获取，或者从DB中获取，从redis中查看当前是谁的购物车
            List<CartInfo> cartInfoList = null;
            // 会有合并的操作，cookie 中的数据没有，应该删除！ cookie 跟 redis 合并
            // 先获取cookie 中的数据
            List<CartInfo> cartListCK = cartCookieHandler.getCartList(request);
            //判断
            if(cartListCK!=null && cartListCK.size()>0){
                //写一个方法将cookie和redis中的数据合并
                cartInfoList = cartService.mergeToCartList(cartListCK,userId);
                //cookie中要删除数据
                cartCookieHandler.deleteCartCookie(request,response);
            }else{
                //cookie中没有数据，直接从redis中获取
                cartInfoList = cartService.getCartList(userId);
            }
            //保存数据，在页面上获取
            request.setAttribute("cartInfoList",cartInfoList);
        }else{
            //用户没有登录，从cookie中获取，只是查看，不能做其他操作
            List<CartInfo> cartList = cartCookieHandler.getCartList(request);
            //保存数据，存入的属性的key要和登录时的保存的key一致
            request.setAttribute("cartInfoList",cartList);
        }
        //返回数据列表页面
        return "cartList";
    }

}
