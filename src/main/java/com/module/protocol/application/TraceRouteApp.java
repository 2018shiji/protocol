package com.module.protocol.application;

import com.module.protocol.IProtocol;
import com.module.protocol.ProtocolManager;
import com.module.protocol.icmp.ICMPProtocolLayer;
import com.module.protocol.udp.UDPProtocolLayer;
import com.module.protocol.utils.HexConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Random;

@Component
public class TraceRouteApp extends Application {
    private byte[] route_ip = null;
    private byte time_to_live =1;
    private short identifier = 0;//进程号
    private short sequence = 0;//消息序列号

    private static byte ICMP_TIME_EXCEEDED_TYPE = 1;
    private static byte ICMP_TIME_EXCEEDED_CODE = 0;

    @Autowired ProtocolManager protocolManager;

    public TraceRouteApp(){
        Random rand = new Random();
        identifier = (short) (50 & 0x0000FFFF);
        port = identifier;
        try{
            //todo 测试百度或者其他网站的路由追踪，目前暂不支持直接域名，需把域名转为ip，可Windows下tracert查看域名具体IP号
            this.route_ip = InetAddress.getByName("192.168.50.1").getAddress();
        } catch (UnknownHostException e){e.printStackTrace();}

    }

    public void startTraceRoute(){
        try{
            byte[] packet = createPackage();
            System.out.println("TraceRouteApp: route_ip = " + HexConversion.bytes2Ipv4(route_ip));
            protocolManager.sendData(packet, route_ip);

            protocolManager.registerForReceiverICMP(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] createPackage() throws Exception {
        byte[] icmpHeader = createICMPEchoHeader();
        if(icmpHeader == null)
            throw new Exception("UDP Header create fail");

        byte[] ipHeader = createIP4Header(icmpHeader.length);

        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        for(int i = 0; i < ipHeader.length; i++){
            System.out.print(Integer.toHexString(ipHeader[i] & 0xFF) + "\t");
        }
        System.out.println();
        for(int i = 0; i < icmpHeader.length; i++){
            System.out.print(Integer.toHexString(icmpHeader[i] & 0xFF) + "\t");
        }
        System.out.println();
        System.out.println("TraceApp: ipHeader length " + ipHeader.length + "\t" + "udpHeader length " + icmpHeader.length);

        //分别构建IP包头和UDP包头后，将两个包头结合在一起
        byte[] packet = new byte[ipHeader.length + icmpHeader.length];
        ByteBuffer packetBuffer = ByteBuffer.wrap(packet);
        packetBuffer.put(ipHeader);
        packetBuffer.put(icmpHeader);

        return packetBuffer.array();
    }

    private byte[] createIP4Header(int dataLength) {
        IProtocol ip4Proto = protocolManager.getProtocol("ip");
        if(ip4Proto == null || dataLength <= 0)
            return null;
        //创建IP包头默认情况下只需要发送数据长度，下层协议号，接收方地址
        HashMap<String, Object> headerInfo = new HashMap();
        headerInfo.put("data_length", dataLength);
        try {
            ByteBuffer destIP = ByteBuffer.wrap(InetAddress.getByName("185.53.178.50").getAddress());
            headerInfo.put("destination_ip", destIP.getInt());
        }catch(UnknownHostException e){
            e.printStackTrace();
        }
        byte protocol = ICMPProtocolLayer.PROTOCOL_ICMP;
        headerInfo.put("protocol", protocol);
        headerInfo.put("identification", (short)port);
        //该值必须依次递增
        headerInfo.put("time_to_live", time_to_live);
        byte[] ipHeader = ip4Proto.createHeader(headerInfo);

        return ipHeader;
    }

    private byte[] createICMPEchoHeader(){
        IProtocol icmp = protocolManager.getProtocol("icmp");

        //构造icmp echo包头
        HashMap<String, Object> headerInfo = new HashMap();
        headerInfo.put("header", "echo");
        headerInfo.put("identifier", identifier);
        headerInfo.put("sequesce_number", sequence);
        sequence++;
        long data = 0;
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(data);
        byte[] dataBuffer = buffer.array();
        headerInfo.put("data", dataBuffer);
        byte[] icmpEchoHeader = icmp.createHeader(headerInfo);

        return icmpEchoHeader;

    }

    @Override
    public void handleData(HashMap<String, Object> data) {
        if(data.get("type") == null || data.get("code") == null){
            return;
        }

        if((byte)data.get("type") != ICMP_TIME_EXCEEDED_TYPE || (byte)data.get("code") != ICMP_TIME_EXCEEDED_CODE){
            return;/** 收到的不是icmp_time_exceeded类型的消息，直接返回 */
        }

        //获得发送该数据包的路由器ip
        byte[] source_ip = HexConversion.ipv42Bytes((String)data.get("source_ip"));
        try{
            String routerIP = InetAddress.getByAddress(source_ip).toString();
            System.out.println("ip of the " + time_to_live + "the router in sending route is: " + routerIP);
            if(!InetAddress.getByName("185.53.178.50").getAddress().equals(source_ip)) {
                time_to_live++;
                startTraceRoute();
            }
        } catch (UnknownHostException e){
            //todo auto-generated catch block
            e.printStackTrace();
        }
    }
}
