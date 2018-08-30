package com.atguigu.gmall.manager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author wangyanjie
 * @create 2018-08-24 - 13:13
 */
@Controller
public class ManagerController {

    @RequestMapping("index")
    public String index(){
        return "index";
    }

    @RequestMapping("attrListPage")
    public String AttrListPage(){
        return "attrListPage";
    }
}
