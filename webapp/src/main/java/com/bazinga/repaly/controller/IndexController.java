package com.bazinga.repaly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author yunshan
 * @version 2018/8/6 14:30
 */
@Controller
@RequestMapping("/")
public class IndexController {

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index() {

        return "index";
    }
    @RequestMapping(value = "/haha", method = RequestMethod.GET)
    public String haha() {
        System.out.println("11111");
        return "index";
    }
}
