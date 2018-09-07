package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.constant.CartConst;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;


/**
 * @author wangyanjie
 * @create 2018-09-05 - 18:37
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private ManageService manageService;
    
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        //先判断购物车中是否有该商品,获取cartInfo对象
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        //查询
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);
        //判断
        if(cartInfoExist!=null){
            //购物车中存在该商品，需要将skuNum+1
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
            //更新数据库的数据
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        }else{
            //购物车中没有该商品，需要新增，cartInfo中的数据来自于skuInfo
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();
            //保存数据

//            String userId;
//            String skuId;
//            BigDecimal cartPrice;
//            Integer skuNum;
//            String imgUrl;
//            String skuName;
//            BigDecimal skuPrice;
//            String isChecked="0";
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuId(skuId);
            cartInfo1.setSkuNum(skuNum);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setSkuName(skuInfo.getSkuName());

            //放入数据库
            cartInfoMapper.insertSelective(cartInfo1);
            //修改缓存
            cartInfoExist = cartInfo1;
        }
        
        //获取jedis,放入数据
        Jedis jedis = redisUtil.getJedis();
        //key,field,value
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        //key==user:userId:cart  field==skuId  value==cartInfo
        jedis.hset(userCartKey, skuId, JSON.toJSONString(cartInfoExist));

        //更新购物车过期时间：用户登录的过期时间
        //redis中获取过期时间的方法ttl(key)
        String userInfoKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        Long ttl = jedis.ttl(userInfoKey);
        //为指定的key设置生存时间
        jedis.expire(userCartKey,ttl.intValue());
        //关闭jedis
        jedis.close();
    }

    /**
     * 整合redis和cookie中的数据
     * @param cartListCK
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListCK, String userId) {
        //根据userId查询到数据库中的cartInfo集合
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        //对cookie中的cart循环遍历
        for (CartInfo cartInfoCK : cartListCK) {
            boolean isMatch = false;
            //对DB中的cart循坏遍历
            for (CartInfo cartInfoDB : cartInfoListDB) {
                //判断DB中的skuId是否和CK中的skuId一致
                if(cartInfoCK.getSkuId().equals(cartInfoDB.getSkuId())){
                    //两者的skuId相同，说明是同一件商品，将两者的skuNum相加，存到数据库
                    cartInfoDB.setSkuNum(cartInfoCK.getSkuNum()+cartInfoDB.getSkuNum());
                    //更新数据库
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch = true;
                }
            }

            //如果DB中没有购物车，直接将CK中的购物车添加到数据库
            if(!isMatch){
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
        }

        //在新的数据库中查询结果并返回
        List<CartInfo> cartInfoList = loadCartCache(userId);

        return cartInfoList;
    }

    /**
     * 查看购物车集合列表
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getCartList(String userId) {
        //从redis中获取,先获取jedis
        Jedis jedis = redisUtil.getJedis();
        //拼接key  user:userId:cart
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        //将hash中的值一次性取出，使用hvals()方法
        List<String> cartJsons = jedis.hvals(userCartKey);
        //定义一个集合
        ArrayList<CartInfo> cartInfoList = new ArrayList<>();
        //判断循环遍历
        if(cartJsons!=null && cartJsons.size()>0){
            for (String cartJson : cartJsons) {
                // cartJson 对应的是每一个skuId 的值， 将cartJson 转换成我们的cartInfo对象
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            //做个排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });
            return cartInfoList;
        }else{
            //从数据库中查询，其中cart_price 可能是旧值，所以需要关联sku_info 表信息。
            //缓存中没有数据
            List<CartInfo> cartInfoListDB = loadCartCache(userId);
            return cartInfoListDB;
        }
    }

    @Override
    public List<CartInfo> loadCartCache(String userId) {
        //根据userId查询cartInfo的信息
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        //判断
        if(cartInfoList==null || cartInfoList.size()==0){
            return null;
        }

        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Map<String,String> map = new HashMap<>(cartInfoList.size());
        for (CartInfo cartInfo : cartInfoList) {
            String newCartInfo = JSON.toJSONString(cartInfo);
            map.put(cartInfo.getSkuId(),newCartInfo);
        }
        //将数据一次性存储到redis中
        jedis.hmset(userCartKey,map);
        //关闭jedis
        jedis.close();
        return cartInfoList;
    }
}
