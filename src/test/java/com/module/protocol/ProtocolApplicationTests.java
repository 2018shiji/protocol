package com.module.protocol;

import com.module.protocol.application.PingApp;
import com.module.protocol.arp.ARPProtocolLayer;
import com.module.protocol.datalink.DataLinkLayer;
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootTest
class ProtocolApplicationTests {

    @Autowired
    PingApp pingApp;
    @Autowired
    DataLinkLayer linkLayer;
    @Autowired
    ARPProtocolLayer arpLayer;

    JpcapCaptor jpcapCaptor;


    void beforeAllTest() {
        NetworkInterface[] devices = JpcapCaptor.getDeviceList();
        NetworkInterface device = null;
        System.out.println("共有" + devices.length + "块网卡");
        for(int i = 0; i < devices.length; i++){
            for(NetworkInterfaceAddress temp : devices[i].addresses){
                System.out.println(temp.address.getHostAddress());
                if("192.168.50.74".equals(temp.address.getHostAddress())){
                    device = devices[i];
                    System.out.println("---------------find-------------");
                    break;
                }
            }
        }
        try {
            jpcapCaptor = JpcapCaptor.openDevice(device, 2000, true, 20);
        }catch(IOException e){e.printStackTrace();}
        linkLayer.initWithOpenDevice(device);
    }

    @Test
    void testArp(){
        beforeAllTest();
        linkLayer.registerPacketReceiver(arpLayer);
        try {
            byte[] ip = InetAddress.getByName("192.168.50.1").getAddress();
            arpLayer.getMacByIP(ip, linkLayer);
        } catch(UnknownHostException e) { e.printStackTrace(); }
        jpcapCaptor.loopPacket(-1, linkLayer);
    }

    @Test
    void testPing() {
        beforeAllTest();
        pingApp.startPing();
        jpcapCaptor.loopPacket(-1, linkLayer);
    }

}