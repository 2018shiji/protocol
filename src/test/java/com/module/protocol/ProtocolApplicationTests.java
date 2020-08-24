package com.module.protocol;

import com.module.protocol.application.appImpl.DHCPApp;
import com.module.protocol.application.appImpl.PingApp;
import com.module.protocol.application.appImpl.TraceRouteApp;
import com.module.protocol.application.tool.PortScan;
import com.module.protocol.application.tool.ScanInfo;
import com.module.protocol.application.tool.ScanInfoInput;
import com.module.protocol.application.tool.SelectorHandler;
import com.module.protocol.arp.ARPProtocolLayer;
import com.module.protocol.datalink.DataLinkLayer;
import com.module.protocol.product.DeviceMonitor;
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
class ProtocolApplicationTests {

    @Autowired JpcapCaptor jpcapCaptor;
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
    }

    @Test
    void testPing() {
        beforeAllTest();
        pingApp.startPing("192.168.50.1", "10.28.56.121");
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

    @Test
    void testPortScan() throws IOException{
        BlockingQueue<ScanInfo> scanInfos = new LinkedBlockingDeque<>(200);
        AtomicInteger count = new AtomicInteger(0);
        new Thread(new ScanInfoInput(scanInfos)).start();
        Selector selector = Selector.open();
        new Thread(new SelectorHandler(selector, count)).start();
        for(int i = 0; i < 50; i++){
            new PortScan(scanInfos, selector, count).start();
        }
    }

    @Autowired
    DeviceMonitor deviceMonitor;

    @Test
    void testDeviceMonitor() throws Exception {
        deviceMonitor.doPingJob(null);
        jpcapCaptor.loopPacket(-1, dataLinkLayer);
    }
}
