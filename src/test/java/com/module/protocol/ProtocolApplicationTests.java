package com.module.protocol;

import com.module.protocol.application.DHCPApp;
import com.module.protocol.application.PingApp;
import com.module.protocol.application.TraceRouteApp;
import com.module.protocol.arp.ARPProtocolLayer;
import com.module.protocol.datalink.DataLinkLayer;
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootTest
class ProtocolApplicationTests {

    @Autowired PingApp pingApp;
    @Autowired ARPProtocolLayer arpLayer;
    @Autowired TraceRouteApp traceRouteApp;
    @Autowired DHCPApp dhcpApp;

    JpcapCaptor jpcapCaptor;

    void beforeAllTest() {
        NetworkInterface[] devices = JpcapCaptor.getDeviceList();
        NetworkInterface device = null;
        System.out.println("共有" + devices.length + "块网卡");
        for(int i = 0; i < devices.length; i++){
            for(NetworkInterfaceAddress temp : devices[i].addresses){
                System.out.println(i + temp.address.getHostAddress());
                if("192.168.43.110".equals(temp.address.getHostAddress())){
                    device = devices[i];
                    System.out.println("---------------find-------------");
                    break;
                }
            }
        }
        DataLinkLayer.getInstance().initWithOpenDevice(device);
        try {
            jpcapCaptor = JpcapCaptor.openDevice(device, 2000, true, 20);
        }catch(IOException e){e.printStackTrace();}
    }

    @Test
    void testPing() {
        beforeAllTest();
        pingApp.startPing();
        jpcapCaptor.loopPacket(-1, DataLinkLayer.getInstance());
    }

    @Test
    void testTraceRoute(){
        beforeAllTest();
        traceRouteApp.startTraceRoute();
        jpcapCaptor.loopPacket(-1, DataLinkLayer.getInstance());
    }

    @Test
    void testDHCP(){
        beforeAllTest();
        dhcpApp.dhcpRequest();
        jpcapCaptor.loopPacket(-1, DataLinkLayer.getInstance());
    }


}
