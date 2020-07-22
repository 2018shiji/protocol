package com.module.protocol.application;

import com.module.protocol.icmp.ICMPProtocolLayer;
import com.module.protocol.IProtocol;
import com.module.protocol.ProtocolManager;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Random;

@Component
public class PingApp extends Application {
    @Setter
    private int echo_times = 1;//连续发送多少次数据包
    @Setter
    private byte[] destIP = null;//ping的对象
    private short identifier = 0;//进程号
    private short sequence = 0;//消息序列

    @Autowired
    private ProtocolManager manager;

    public PingApp() {
        Random rand = new Random();
        identifier = (short) (50 & 0x0000FFFF);
        port = identifier;
        try {
            this.destIP = InetAddress.getByName("192.168.50.1").getAddress();
        } catch (UnknownHostException e){e.printStackTrace();}

    }

    public void startPing() {
        for(int i = 0; i < echo_times; i++){
            try{
                byte[] packet = createPackage();
                manager.sendData(packet, destIP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] createPackage() throws Exception {
        byte[] icmpEchoHeader = createICMPEchoHeader();
        if(icmpEchoHeader == null){
            throw new Exception("ICMP Header create fail");
        }
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
        try {
            ByteBuffer destIP = ByteBuffer.wrap(InetAddress.getByName("185.53.178.50").getAddress());
            headerInfo.put("destination_ip", destIP.getInt());
        }catch (UnknownHostException e){
            e.printStackTrace();
        }

        byte protocol = ICMPProtocolLayer.PROTOCOL_ICMP;
        headerInfo.put("protocol", protocol);

        headerInfo.put("identification", (short)port);

        byte[] ipHeader = ip4.createHeader(headerInfo);

        return ipHeader;
    }

    @Override
    public void handleData(HashMap<String, Object> data) {
        long time = System.currentTimeMillis();
        short sequence = (short)data.get("sequence");
        byte[] time_buf = (byte[])data.get("data");
        ByteBuffer buf = ByteBuffer.wrap(time_buf);
        long send_time = buf.getLong();
        System.out.println("￥￥￥￥￥￥￥￥￥ receive reply for ping request " + sequence);
    }

}
