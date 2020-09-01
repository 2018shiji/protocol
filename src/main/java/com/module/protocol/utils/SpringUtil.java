package com.module.protocol.utils;

import com.module.protocol.layer.datalink.DataLinkLayer;
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Configuration
public class SpringUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(SpringUtil.applicationContext == null){
            SpringUtil.applicationContext = applicationContext;
        }
    }

    public static <T> T getBean(Class<T> clazz){
        return applicationContext.getBean(clazz);
    }

    @Autowired
    DataLinkLayer dataLinkLayer;

    @Bean
    public JpcapCaptor jpcapCaptor(){
        JpcapCaptor jpcapCaptor = null;
        NetworkInterface[] devices = JpcapCaptor.getDeviceList();
        NetworkInterface device = null;
        System.out.println("共有" + devices.length + "块网卡");
        for(int i = 0; i < devices.length; i++){
            for(NetworkInterfaceAddress temp : devices[i].addresses){
//                System.out.println(i + temp.address.getHostAddress());
                if("192.168.50.74".equals(temp.address.getHostAddress())){
                    device = devices[i];
                    System.out.println("---------------find-------------");
                    break;
                }
            }
        }
        dataLinkLayer.initWithOpenDevice(device);
        try {
            jpcapCaptor = JpcapCaptor.openDevice(device, 2000, true, 20);
        }catch(IOException e){e.printStackTrace();}
        return jpcapCaptor;
    }
}
