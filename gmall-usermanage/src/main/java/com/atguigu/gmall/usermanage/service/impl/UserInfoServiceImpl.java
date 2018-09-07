package com.atguigu.gmall.usermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.usermanage.mapper.UserAddressMapper;
import com.atguigu.gmall.usermanage.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;


import java.util.List;

/**
 * @author wangyanjie
 * @create 2018-08-22 - 18:34
 */
@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60;


    @Override
    public List<UserInfo> findAll() {
        List<UserInfo> userInfoList = userInfoMapper.selectAll();
        return userInfoList;
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setId(userId);
        List<UserAddress> userAddressList = userAddressMapper.select(userAddress);
        return userAddressList;
    }

    @Override
    public UserInfo login(UserInfo userInfo) {

        //将密码进行加密
        String password = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        //将加密的密码存入
        userInfo.setPasswd(password);
        //查询用户信息
        UserInfo info = userInfoMapper.selectOne(userInfo);
        //判断
        if(info!=null){
            //用户存在，获得redis，将用户信息存入到redis中
            Jedis jedis = redisUtil.getJedis();
            jedis.setex(userKey_prefix+info.getId()+userinfoKey_suffix,userKey_timeOut, JSON.toJSONString(info));
            jedis.close();
            return info;
        }
        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        //获取jedis对象
        Jedis jedis = redisUtil.getJedis();
        //拼接key(user:userId:info)user:2:info
        String key = userKey_prefix+userId+userinfoKey_suffix;
        //获取redis中的用户登录的信息
        String userJson = jedis.get(key);

        //防止过期要延长时效，其他模块只需要调用验证方法(在别的模块意味着重新登录，
        // 但是不会去数据库中查找，而是在redis缓存中调用验证方法自行登录)
        jedis.expire(key,userKey_timeOut);
        //不为空，说明redis中存在
        if(userJson!=null && userJson.length()>0){
            //将userJson转换为userInfo对象
            UserInfo userInfo = JSON.parseObject(userJson,UserInfo.class);
            //将结果集返回
            return userInfo;
        }
        return null;
    }
}
