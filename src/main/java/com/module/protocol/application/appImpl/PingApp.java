package com.module.protocol.application.appImpl;

import com.module.protocol.application.AppDataEvent;
import com.module.protocol.application.Application;
import com.module.protocol.application.ApplicationGroup;
import com.module.protocol.icmp.ICMPProtocolLayer;
import com.module.protocol.IProtocol;
import com.module.protocol.ProtocolManager;
import com.module.protocol.utils.HexConversion;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class PingApp extends Application {
    private short identifier = 0;//进程号
    private short sequence = 0;//消息序列
    private byte[] finalRemoteIP;

    @Autowired ProtocolManager manager;
    @Autowired ApplicationGroup appGroup;

    @PostConstruct
    public void initPingApp() {
        identifier = (short) (50 & 0x0000FFFF);
        appGroup.registerToICMPList(this);
    }

    public void startPing(String netInterface, String finalRemoteAddr) {

        try {
            /** 局域网内IP互ping时，destIP采用远程机器IP即可，公网互ping时需借助网关对外发起ping包请求 **/
//            byte[] destIP = InetAddress.getByName("192.168.43.154").getAddress();
            this.finalRemoteIP = InetAddress.getByName(finalRemoteAddr).getAddress();
            byte[] netInterfaceIP = InetAddress.getByName(netInterface).getAddress();
            byte[] packet = createPackage();
            manager.sendData(packet, netInterfaceIP);
        } catch (Exception e){e.printStackTrace();}

    }

    private byte[] createPackage() {
        byte[] icmpEchoHeader = createICMPEchoHeader();
        byte[] ipHeader =createIP4Header(icmpEchoHeader.length);
        for(int i = 0; i < ipHeader.length; i++) {
            System.out.print(Integer.toHexString(ipHeader[i] & 0xFF) + "\t");
        }
        System.out.println();
        for(int i = 0; i < icmpEchoHeader.length; i++) {
            System.out.print(Integer.toHexString(icmpEchoHeader[i] & 0xFF) + "\t");
        }
        System.out.println();
        System.out.println("PingApp: ipHeader length " + ipHeader.length + "****** icmpHeader length" + icmpEchoHeader.length);

        //分别构建ip包头和icmp echo包头后，将两个包头结合在一起
        byte[] packet = new byte[ipHeader.length + icmpEchoHeader.length];
        ByteBuffer packetBuffer = ByteBuffer.wrap(packet);
        packetBuffer.put(ipHeader);
        packetBuffer.put(icmpEchoHeader);

        return packetBuffer.array();
    }

    private byte[] createICMPEchoHeader() {
        IProtocol icmp = manager.getProtocol("icmp");

        //构造icmp echo包头
        HashMap<String, Object> headerInfo = new HashMap<>();
        headerInfo.put("header", "echo");
        headerInfo.put("identifier", identifier);
        headerInfo.put("sequence_number", sequence);
        sequence++;
        //附带当前时间
        long time = 0;
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(time);
        byte[] timeBuffer = buffer.array();
        headerInfo.put("data", timeBuffer);
        byte[] icmpEchoHeader = icmp.createHeader(headerInfo);

        return icmpEchoHeader;
    }

    private byte[] createIP4Header(int dataLength) {
        IProtocol ip4 = manager.getProtocol("ip");
        if(ip4 == null || dataLength <= 0)
            return null;
        //创建IP包头默认情况下只需要发送数据长度，下层协议号，接收方IP地址
        HashMap<String, Object> headerInfo = new HashMap<>();
        headerInfo.put("data_length", dataLength);
        ByteBuffer finalDestIP = ByteBuffer.wrap(finalRemoteIP);
        headerInfo.put("destination_ip", finalDestIP.getInt());

        byte protocol = ICMPProtocolLayer.PROTOCOL_ICMP;
        headerInfo.put("protocol", protocol);


        byte[] ipHeader = ip4.createHeader(headerInfo);

        return ipHeader;
    }

    @Override
    public void handleData(AppDataEvent event) {
        Map<String, Object> data = event.getHeaderInfo();
        //获得发送该数据包的路由器ip
        byte[] source_ip = HexConversion.ipv42Bytes((String)data.get("source_ip"));
        try {
            System.out.println("￥￥￥￥￥￥￥￥￥ receive reply for ping request from: "
                    + InetAddress.getByAddress(source_ip).toString()
            );
        } catch (Exception e){e.printStackTrace();}
    }

}
