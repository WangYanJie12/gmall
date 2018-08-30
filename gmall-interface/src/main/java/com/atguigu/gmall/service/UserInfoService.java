package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

/**
 * @author wangyanjie
 * @create 2018-08-22 - 18:21
 */
public interface UserInfoService {

    /**
     * 查询所有用户信息
     * @return
     */
    List<UserInfo> findAll();

    /**
     * 根据用户的id查询用户的住址信息
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddressList(String userId);
}
