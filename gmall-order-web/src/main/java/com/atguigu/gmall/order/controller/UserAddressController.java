package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author wangyanjie
 * @create 2018-08-22 - 19:38
 */
@Controller
public class UserAddressController {

    @Reference
    private UserInfoService userInfoService;

    @RequestMapping("order")
    @ResponseBody
    public List<UserAddress> getUserAddressList(String userId){
        List<UserAddress> userAddressList = userInfoService.getUserAddressList(userId);
        return userAddressList;
    }
}
