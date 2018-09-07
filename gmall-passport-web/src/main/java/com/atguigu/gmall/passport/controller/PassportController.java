package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.passport.util.JwtUtil;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangyanjie
 * @create 2018-09-04 - 17:03
 */
@Controller
public class PassportController {

    @Reference
    private UserInfoService userInfoService;

    //获取配置文件中的值要用value注解
    @Value("${token.key}")
    String signKey;

    @RequestMapping("index")
    public String index(HttpServletRequest request){

        //获取页面中的originUrl
        String originUrl = request.getParameter("originUrl");
        //将从页面中获取到的originUrl存到request域中
        request.setAttribute("originUrl",originUrl);

        //返回到index.html页面
        return "index";
    }


    @RequestMapping("login")
    @ResponseBody
    public String login(HttpServletRequest request, UserInfo userInfo){

        //获取ip地址
        String ip = request.getHeader("X-forwarded-for");
        //如果用户信息存在
        if(userInfo!=null){
            //获得登录用户信息
            UserInfo loginUser = userInfoService.login(userInfo);
            if(loginUser!=null){
                //生成token
                Map map = new HashMap<>();
                //将userID和NickName存到map集合中，页面上要用到这两个属性的值
                map.put("userId",loginUser.getId());
                map.put("nickName",loginUser.getNickName());

                //生成token
                String token = JwtUtil.encode(signKey,map,ip);
                return token;
            } else{
                return "fail";
            }

        }
        return "fail";
    }

    //登录验证
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        //获取token
        String token = request.getParameter("token");
        //获取salt
        String salt = request.getHeader("X-forwarded-for");
        //解码
        Map<String, Object> map = JwtUtil.decode(token, signKey, salt);
        if(map!=null && map.size()>0){
            //map中的userId和redis中的进行匹配
            String userId = (String) map.get("userId");
            //验证userId是否匹配
            UserInfo userInfo = userInfoService.verify(userId);
            if(userInfo!=null){
                return "success";
            }else{
                return "fail";
            }
        }
        return "fail";
    }

}
