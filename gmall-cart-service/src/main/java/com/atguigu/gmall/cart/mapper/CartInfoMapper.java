package com.atguigu.gmall.cart.mapper;

import com.atguigu.gmall.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author wangyanjie
 * @create 2018-09-05 - 12:44
 */
public interface CartInfoMapper extends Mapper<CartInfo> {

    /**
     * 根据userId查询cartInfo信息
     * @param userId
     * @return
     */
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
