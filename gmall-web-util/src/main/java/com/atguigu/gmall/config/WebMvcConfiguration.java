package com.atguigu.gmall.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author wangyanjie
 * @create 2018-09-04 - 23:40
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //拦截所有
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
        //将设置好的registry让它生效
        super.addInterceptors(registry);
    }
}
