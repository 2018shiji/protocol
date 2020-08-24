package com.module.protocol;

import com.module.protocol.application.appImpl.PingApp;
import com.module.protocol.datalink.DataLinkLayer;
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Autowired DataLinkLayer dataLinkLayer;

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
