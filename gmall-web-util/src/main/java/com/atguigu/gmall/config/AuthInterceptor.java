package com.atguigu.gmall.config;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author wangyanjie
 * @create 2018-09-04 - 21:37
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    //该方法将在请求处理之前进行调用
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)throws Exception {

        //获取地址栏里面的newToken
        String token = request.getParameter("newToken");
        //判断token是否存在
        if(token!=null){
            //将token保存到cookie中，调用工具类
            //WebConst.COOKIE_MAXAGE:设置cookie的过期时间7*24*3600;一周
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }

        //直接访问登录页面的时候，当用户进入其他项目模块的时候，地址栏获取到的token为空
        if(token==null){
            //调用工具类，获取token的值
            //如果用户登录了，访问其他页面时不会有newToken，可能已经将token保存在了cookie中
            token = CookieUtil.getCookieValue(request,"token",false);
        }

        //此时token已经有值了，即已经登录的token，cookie中的token
        if(token!=null){
            //取token中的有效数据--解密
            Map map = getUserMapByToken(token);

            String nickName = (String) map.get("nickName");
            request.setAttribute("nickName",nickName);
        }


        //获取方法上的注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        //判断
        if(methodAnnotation!=null){
            //说明类上有注解，一定要登录，调用verfy()
            String remoteAddr = request.getHeader("x-forwarded-for");
            //认证控制器在哪个项目，远程调用
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&currentIp=" + remoteAddr);
            //"success"  "fail"
            if("success".equals(result)){
                //说明已经登录，保存userId在购物车中使用
                Map map = getUserMapByToken(token);
                String userId = (String) map.get("userId");
                request.setAttribute("userId",userId);
                return true;
            }else {
                //fail
                if (methodAnnotation.autoRedirect()) {
                    //认证失败，重新登录
                    String requestURL = request.getRequestURL().toString();
                    String encodeUrl = URLEncoder.encode(requestURL, "UTF-8");
                    //重定向到登录页面
                    response.sendRedirect(WebConst.LOGIN_ADDRESS + "?originUrl=" + encodeUrl);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 解密token
     * @param token
     * @return
     */
    private Map getUserMapByToken(String token) {
        //http://item.gmall.com/28.html?
        // newToken=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0.WUvbFvXQnTMBGNyHWT-DE41MR9cn7c_W1oAtDAzb7VU
        //token中的有效数据是eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0---即在PassportController中的map

        //调用工具类将newToken中的字符串进行切割，切出来我们需要的map
        String tokenUserInfo = StringUtils.substringBetween(token, ".");

        //使用解密类
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        //将map进行解密
        byte[] tokenBytes = base64UrlCodec.decode(tokenUserInfo);
        //将数组要转为map集合，不能实现，要先将数组转为字符串
        String tokenJson = null;
        try {
            //将数组转为字符串
            tokenJson = new String(tokenBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //将字符串转为map集合并返回
        Map map = JSON.parseObject(tokenJson, Map.class);

        return map;
    }
}
