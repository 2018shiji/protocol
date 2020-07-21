package com.module.protocol.application;

import com.module.protocol.IProtocol;
import com.module.protocol.ProtocolManager;
import com.module.protocol.udp.UDPProtocolLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.HashMap;

@Component
public class TraceRouteApp extends Application {
    private char dest_port = 33434;
    private byte[] dest_ip = null;
    private byte time_to_live =1;

    private static byte ICMP_TIME_EXCEEDED_TYPE = 1;
    private static byte ICMP_TIME_EXCEEDED_CODE = 0;

    @Autowired
    ProtocolManager protocolManager;

    public void startTraceRoute(){
        try{
            byte[] packet = createPackage();
            //todo dest_ip 目前为null
            System.out.println("TraceRouteApp: dest_ip = " + dest_ip);
            protocolManager.sendData(packet, dest_ip);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] createPackage() throws Exception {
        byte[] udpHeader = createUDPHeader();
        if(udpHeader == null)
            throw new Exception("UDP Header create fail");

        byte[] ipHeader = createIP4Header(udpHeader.length);

        //分别构建IP包头和UDP包头后，将两个包头结合在一起
        byte[] packet = new byte[ipHeader.length + udpHeader.length];
        ByteBuffer packetBuffer = ByteBuffer.wrap(packet);
        packetBuffer.put(ipHeader);
        packetBuffer.put(udpHeader);

        return packetBuffer.array();
    }

    private byte[] createIP4Header(int dataLength) {
        IProtocol ip4Proto = protocolManager.getProtocol("ip");
        if(ip4Proto == null || dataLength <= 0)
            return null;
        //创建IP包头默认情况下只需要发送数据长度，下层协议号，接收方地址
        HashMap<String, Object> headerInfo = new HashMap();
        headerInfo.put("data_length", dataLength);
        ByteBuffer destIP = ByteBuffer.wrap(dest_ip);
        headerInfo.put("destination_ip", destIP.getInt());
        byte protocol = UDPProtocolLayer.PROTOCOL_UDP;
        headerInfo.put("protocol", protocol);
        headerInfo.put("identification", (short)port);
        //该值必须依次递增
        headerInfo.put("time_to_live", time_to_live);
        byte[] ipHeader = ip4Proto.createHeader(headerInfo);

        return ipHeader;
    }

    private byte[] createUDPHeader(){
        IProtocol udpProto = protocolManager.getProtocol("udp");
        if(udpProto == null)
            return null;

        HashMap<String, Object> headerInfo = new HashMap();
        char udpPort = (char)port;
        headerInfo.put("source_port", udpPort);
        headerInfo.put("dest_port", dest_port);

        byte[] data = new byte[24];
        headerInfo.put("data", data);

        return udpProto.createHeader(headerInfo);
    }
}
