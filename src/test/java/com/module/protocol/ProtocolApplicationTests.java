package com.module.protocol;

import com.module.protocol.application.product.DHCPApp;
import com.module.protocol.application.product.PingApp;
import com.module.protocol.application.product.TraceRouteApp;
import com.module.protocol.arp.ARPProtocolLayer;
import com.module.protocol.datalink.DataLinkLayer;
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class ProtocolApplicationTests {

    JpcapCaptor jpcapCaptor;
    @Autowired DataLinkLayer dataLinkLayer;

    @Autowired PingApp pingApp;
    @Autowired ARPProtocolLayer arpLayer;
    @Autowired TraceRouteApp traceRouteApp;
    @Autowired DHCPApp dhcpApp;


    void beforeAllTest() {
        NetworkInterface[] devices = JpcapCaptor.getDeviceList();
        NetworkInterface device = null;
        System.out.println("共有" + devices.length + "块网卡");
        for(int i = 0; i < devices.length; i++){
            for(NetworkInterfaceAddress temp : devices[i].addresses){
//                System.out.println(i + temp.address.getHostAddress());
                if("192.168.43.110".equals(temp.address.getHostAddress())){
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
    }

    @Test
    void testPing() {
        beforeAllTest();
        pingApp.startPing();
        jpcapCaptor.loopPacket(-1, dataLinkLayer);
    }

    @Test
    void testTraceRoute(){
        beforeAllTest();
        traceRouteApp.startTraceRoute();
        jpcapCaptor.loopPacket(-1, dataLinkLayer);
    }

    @Test
    void testDHCP(){
        beforeAllTest();
        dhcpApp.dhcpRequest();
        jpcapCaptor.loopPacket(-1, dataLinkLayer);
    }


}
