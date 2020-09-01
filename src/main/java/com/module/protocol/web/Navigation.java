package com.module.protocol.web;

import com.module.protocol.websocket.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Navigation {
    @Autowired WebSocket webSocket;

    @ResponseBody
    @RequestMapping("/testPingApp")
    public String doGetPingApp(){
        return "success";
    }

    @ResponseBody
    @RequestMapping("/sendWebSocketSingle/{userName}")
    public String sendWebSocketSingle(@PathVariable("userName") String userName){
        String text=userName+" 你好！ 这是websocket单人发送！";
        webSocket.sendOneMessage(userName,text);
        return text;
    }

    @ResponseBody
    @RequestMapping("/sendWebSocketAll")
    public String sendWebSocketAll(){
        String text="你们好！这是websocket群体发送！";
        webSocket.sendAllMessage(text);
        return text;
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
