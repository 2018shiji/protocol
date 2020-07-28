package com.module.protocol;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Navigation {

    @ResponseBody
    @RequestMapping("/testPingApp")
    public String doGetPingApp(){
        return "success";
    }

    @ResponseBody
    @RequestMapping("/testTraceRootApp")
    public String doGetTraceRootApp(){
        return "success";
    }

    @ResponseBody
    @RequestMapping("/testDHCPApp")
    public String doGetDHCPApp(){
        return "success";
    }

    @ResponseBody
    @RequestMapping("/testDNSApp")
    public String doGetDNSApp(){
        return "success";
    }
}
