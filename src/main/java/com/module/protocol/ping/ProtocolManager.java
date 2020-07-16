package com.module.protocol.ping;

import com.module.protocol.application.ApplicationManager;
import com.module.protocol.application.IApplication;
import com.module.protocol.arp.ARPProtocolLayer;
import com.module.protocol.datalink.DataLinkLayer;
import com.module.protocol.icmp.ICMPProtocolLayer;
import com.module.protocol.utils.IMacReceiver;
import jpcap.PacketReceiver;
import jpcap.packet.EthernetPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;

/**
 * https://blog.csdn.net/tyler_download/article/details/86638606
 */
@Component
public class ProtocolManager implements PacketReceiver, IMacReceiver {
    @Autowired
    private ARPProtocolLayer arpLayer;
    @Autowired
    private DataLinkLayer dataLink;

    @Autowired
    @Qualifier("ipProtocolLayer")
    private IProtocol ipProtocolLayer;

    @Autowired
    @Qualifier("icmpProtocolLayer")
    private IProtocol icmpProtocolLayer;

    @Autowired
    private ApplicationManager manager;

    private static HashMap<String, byte[]> ipToMacTable;
    private static HashMap<String, byte[]> dataWaitToSend;

    @PostConstruct
    public void initProtocolManager(){
        ipToMacTable = new HashMap<>();
        dataWaitToSend = new HashMap<>();
        dataLink.registerPacketReceiver(this);
    }

    public IProtocol getProtocol(String name) {
        switch (name.toLowerCase()) {
            case "icmp":
                return new ICMPProtocolLayer();
            case "ip":
                return new IPProtocolLayer();
        }
        return null;
    }

    public void sendData(byte[] data, byte[] ip) throws Exception {
        /**
         * 发送数据前先检查给定ip的mac地址是否存在，
         * 如果没有则先让ARP协议获取mac地址
         */
        byte[] mac = ipToMacTable.get(Arrays.toString(ip));
        if(mac == null) {
            HashMap<String, Object> headerInfo = new HashMap<>();
            headerInfo.put("send_ip", ip);

            arpLayer.getMacByIP(ip, dataLink);
            //将要发送的数据存起，等待mac地址返回后再发送
            dataWaitToSend.put(Arrays.toString(ip), data);
        } else {
            //如果mac地址已经存在则直接发送数据
            dataLink.sendData(data, mac, IPPacket.IPPROTO_IP);
        }
    }

    @Override
    public void receivePacket(Packet packet) {
        if(packet == null)
            return;

        //确保收到的数据包是arp类型
        EthernetPacket etherHeader = (EthernetPacket) packet.datalink;

        //处理IP包头
        if(etherHeader.frametype == EthernetPacket.ETHERTYPE_IP){
            handleIPPacket(packet);
        }
    }

    @Override
    public void receiveMacAddress(byte[] ip, byte[] mac) {
        ipToMacTable.put(Arrays.toString(ip), mac);
        //一旦有mac地址更新后，查看缓存表是否有等待发送的数据
        sendWaitingData(ip);
    }

    private void handleIPPacket(Packet packet) {
        HashMap<String, Object> info = ipProtocolLayer.handlePacket(packet);
        if(info == null)
            return;

        byte protocol = (byte)info.get("protocol");
        //设置下一层协议的头部
        System.out.println("ProtocolManager -> receive packet with protocol: " + info.get("protocol"));
        System.out.println("source_ip: " + info.get("source_ip") + "\t" + "dest_ip: " + info.get("dest_ip"));
        System.out.println("header: " + info.get("header"));
        System.out.println("*************************************************");

        switch(protocol) {
            case IPPacket.IPPROTO_ICMP:
                handleICMPPacket(packet);
                break;
            default:
                return;
        }

    }

    private void handleICMPPacket(Packet packet) {
        HashMap<String, Object> headerInfo = icmpProtocolLayer.handlePacket(packet);
        short identifier = (short)headerInfo.get("identifier");
        IApplication app = manager.getApplicationByPort(identifier);
        if(app != null && app.isClosed() != true)
            app.handleData(headerInfo);
    }

    private void sendWaitingData(byte[] destIP) {
        byte[] data = dataWaitToSend.get(Arrays.toString(destIP));
        byte[] mac = ipToMacTable.get(Arrays.toString(destIP));
        if(data != null && mac != null)
            dataLink.sendData(data, mac, EthernetPacket.ETHERTYPE_IP);
    }

}
