package com.module.protocol;

import com.module.protocol.application.Application;
import com.module.protocol.arp.ARPProtocolLayer;
import com.module.protocol.datalink.DataLinkLayer;
import com.module.protocol.icmp.ICMPProtocolLayer;
import com.module.protocol.ip.IPProtocolLayer;
import com.module.protocol.udp.UDPProtocolLayer;
import com.module.protocol.utils.HexConversion;
import jpcap.PacketReceiver;
import jpcap.packet.EthernetPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * https://blog.csdn.net/tyler_download/article/details/86638606
 */
@Component
public class ProtocolManager implements PacketReceiver {
    private long sendTime;

    private static HashMap<String, byte[]> ipToMacTable;
    private static HashMap<String, byte[]> dataWaitToSend;
    private static ArrayList<Application> icmpPacketReceivers;
    private static byte[] broadcast=new byte[]{(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255};

    @PostConstruct
    public void initProtocolManager(){
        ipToMacTable = new HashMap<>();
        dataWaitToSend = new HashMap<>();
        icmpPacketReceivers = new ArrayList<>();
        DataLinkLayer.getInstance().registerPacketReceiver(this);
    }

    public IProtocol getProtocol(String name) {
        switch (name.toLowerCase()) {
            case "icmp":
                return new ICMPProtocolLayer();
            case "ip":
                return new IPProtocolLayer();
            case "udp":
                return new UDPProtocolLayer();
        }
        return null;
    }

    //增加一个广播IP数据包接口
    public void broadcastData(byte[] data){
        DataLinkLayer.getInstance().sendData(data, broadcast, EthernetPacket.ETHERTYPE_IP);
    }

    public void registerForReceiverICMP(Application receiver){
        if(!icmpPacketReceivers.contains(receiver)){
            icmpPacketReceivers.add(receiver);
        }
    }

    public void sendData(byte[] data, byte[] ip) throws Exception {
        /**
         * 发送数据前先检查给定ip的mac地址是否存在，
         * 如果没有则先让ARP协议获取mac地址
         */
        byte[] mac = ipToMacTable.get(Arrays.toString(ip));
        if(mac == null) {
            if(!ipToMacTable.containsKey(Arrays.toString(ip))) {
                System.out.println("ProtocolManager: send mac request");
                HashMap<String, Object> headerInfo = new HashMap<>();
                headerInfo.put("sender_ip", ip);
                ARPProtocolLayer arpLayer = new ARPProtocolLayer();
                byte[] arpRequest = arpLayer.createHeader(headerInfo);
                DataLinkLayer.getInstance().sendData(arpRequest, broadcast, EthernetPacket.ETHERTYPE_ARP);
            }
            ipToMacTable.put(Arrays.toString(ip), null);
            //将要发送的数据存起，等待mac地址返回后再发送
            dataWaitToSend.put(Arrays.toString(ip), data);
        } else {
            //如果mac地址已经存在则直接发送数据
            System.out.println("ProtocolManager: send ping request by mac");
            sendTime = System.currentTimeMillis();
            DataLinkLayer.getInstance().sendData(data, mac, EthernetPacket.ETHERTYPE_IP);
        }
    }

    @Override
    public void receivePacket(Packet packet) {
        if(packet == null)
            return;

        EthernetPacket etherHeader = (EthernetPacket) packet.datalink;
        /*
         * 数据链路层在发送数据包时会添加一个802.3的以太网包头，格式如下
         * 0-7字节：[0-6]Preamble , [7]start fo frame delimiter
         * 8-22字节: [8-13] destination mac, [14-19]: source mac
         * 20-21字节: type
         * type == 0x0806表示数据包是arp包, 0x0800表示IP包,0x8035是RARP包
         */
        if(etherHeader.frametype == EthernetPacket.ETHERTYPE_ARP){
            //调用ARP协议解析数据包
            ARPProtocolLayer arpLayer = new ARPProtocolLayer();
            HashMap<String, Object> info = arpLayer.handlePacket(packet);
            byte[] senderIP = (byte[]) info.get("sender_ip");
            byte[] senderMac = (byte[]) info.get("sender_mac");
            ipToMacTable.put(Arrays.toString(senderIP), senderMac);
            System.out.println("ProtocolManager: receive ip and mac address " + HexConversion.bytes2Ipv4(senderIP) + senderMac);
            sendWaitingData(senderIP);
        }

        //处理IP包头
        if(etherHeader.frametype == EthernetPacket.ETHERTYPE_IP){
            handleIPPacket(packet);
        }


    }

    private void handleIPPacket(Packet packet) {
        IPProtocolLayer ipProtocolLayer = new IPProtocolLayer();
        HashMap<String, Object> info = ipProtocolLayer.handlePacket(packet);
        if(info == null)
            return;

        byte protocol = 0;
        if(info.get("protocol") != null){
            protocol = (byte)info.get("protocol");
            //设置下一层的头部
            packet.header = (byte[])info.get("header");
        }

        if(IPPacket.IPPROTO_ICMP == protocol) {
            System.out.println("------------after execute------------");
            System.out.println("ProtocolManager -> receive packet with protocol: " + info.get("protocol"));
            System.out.println("source_ip: " + info.get("source_ip") + "\t" + "dest_ip: " + info.get("dest_ip"));
            for(int i = 0; i < ((byte[])info.get("header")).length; i++){
                System.out.print((((byte[])info.get("header"))[i] & 0xFF) + "\t");
            }
            System.out.println();
            for(int i = 0; i < packet.header.length; i++) {
                System.out.print(Integer.toHexString(packet.header[i] & 0xFF) + "\t");
            }
            System.out.println("\n" + "*************************************************");
        }

        if(IPPacket.IPPROTO_UDP == protocol){
            System.out.println("*_**_**_**_**_**_**_**_**_*");
            System.out.println("找到了UDP包");
        }

        switch(protocol) {
            case IPPacket.IPPROTO_ICMP:
                handleICMPPacket(packet, info);
                break;
            case IPPacket.IPPROTO_UDP:
                handleUDPPacket(packet, info);
            default:
                return;
        }

    }

    private void handleICMPPacket(Packet packet, HashMap<String, Object> infoFromUpLayer) {
        ICMPProtocolLayer icmpLayer = new ICMPProtocolLayer();
        HashMap<String, Object> headerInfo = icmpLayer.handlePacket(packet);

        if(headerInfo == null)
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        for(Map.Entry entry : infoFromUpLayer.entrySet()){
            headerInfo.put((String)entry.getKey(), entry.getValue());
        }

        //把收到的icmp数据包发送给所有等待对象
        //trace app注册接收方式
        for(int i = 0; i < icmpPacketReceivers.size(); i++){
            Application receiver = icmpPacketReceivers.get(i);
            receiver.handleData(headerInfo);
        }

        //todo ping app 与 trace app目前使用两种方式进行注册，暂时二者选择其一，后续得整合到一起
        //ping app注册接收方式
//        short identifier = (short)headerInfo.get("identifier");
//        IApplication app = ApplicationManager.getInstance().getApplicationByPort(identifier);
//        if(app != null && app.isClosed() != true)
//            app.handleData(headerInfo);

    }

    private void handleUDPPacket(Packet packet, HashMap<String, Object> infoFromUpLayer){
        IProtocol udpProtocol = new UDPProtocolLayer();
        HashMap<String, Object> headerInfo = udpProtocol.handlePacket(packet);

        short destPort = (short)headerInfo.get("dest_port");
        IApplication app = ApplicationManager.getInstance().getApplicationByPort(destPort);
        app.handleData(headerInfo);
    }

    private void sendWaitingData(byte[] destIP) {
        byte[] data = dataWaitToSend.get(Arrays.toString(destIP));
        System.out.println(HexConversion.bytes2Ipv4(data));
        byte[] mac = ipToMacTable.get(Arrays.toString(destIP));
        if(data != null && mac != null) {
            System.out.println("ProtocolManager: send message to ip: " + HexConversion.bytes2Ipv4(destIP));
            System.out.println("ProtocolManager: message: " + HexConversion.bytes2Ipv4(data));
            sendTime = System.currentTimeMillis();
            DataLinkLayer.getInstance().sendData(data, mac, EthernetPacket.ETHERTYPE_IP);
        }
    }

}
