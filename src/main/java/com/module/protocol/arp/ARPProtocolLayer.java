package com.module.protocol.arp;

import com.module.protocol.datalink.DataLinkLayer;
import com.module.protocol.utils.IMacReceiver;
import jpcap.PacketReceiver;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * https://cloud.tencent.com/developer/article/1375459
 */
@Component
public class ARPProtocolLayer implements PacketReceiver {
    @Autowired
    private DataLinkLayer dataLink;
    private HashMap<byte[], byte[]> ipToMacTable = new HashMap<>();
    private HashMap<Integer, ArrayList<IMacReceiver>>
            ipToMacReceiverTable = new HashMap<>();
    /**
     * 数据包含数据链路层包头:dest_mac(6byte) + source_mac(6byte) + frame_type(2byte)
     * 因此读取ARP数据时需要跳过开头14字节
     */
    private static int ARP_OPCODE_START = 20;
    private static int ARP_SENDER_MAC_START = 22;
    private static int ARP_SENDER_IP_START = 28;
    private static int ARP_TARGET_IP_START = 38;

    @Override
    public void receivePacket(Packet packet) {
        if(packet == null)
            return;

        //确保收到的数据包是arp类型
        EthernetPacket etherHeader = (EthernetPacket)packet.datalink;
        /**
         * 数据链路层在发送数据包时会添加一个802.3的以太网包头，格式如下
         * 0-7字节：[0-6]Preamble , [7]start fo frame delimiter
         * 8-22字节: [8-13] destination mac, [14-19]: source mac
         * 20-21字节: type
         * type == 0x0806表示数据包是arp包, 0x0800表示IP包,0x8035是RARP包
         */
        if (etherHeader.frametype != EthernetPacket.ETHERTYPE_ARP)
            return;

        byte[] header = packet.header;
        analyzeARPMessage(header);
    }

    public void getMacByIP(byte[] ip, IMacReceiver receiver) {
        if(receiver == null)
            return;

        //查看给的ip的mac是否已经缓存
        int ipToInt = ByteBuffer.wrap(ip).getInt();
        if(ipToMacTable.get(ipToInt) != null) {
            receiver.receiveMacAddress(ip, ipToMacTable.get(ipToInt));
        }

        if(ipToMacReceiverTable.get(ipToInt) == null) {
            ipToMacReceiverTable.put(ipToInt, new ArrayList<>());
            //发送ARP请求包
            sendARPRequestMsg(ip);
        }

        ArrayList<IMacReceiver> receivers = ipToMacReceiverTable.get(ipToInt);
        if(receivers.contains(receiver) != true){
            receivers.add(receiver);
        }

        return;
    }


    private boolean analyzeARPMessage(byte[] data) {
        /**
         * 解析获得的APR消息包，从中获得各项信息，此处默认返回的mac地址长度都是6
         */
        //先读取2,3字节，获取消息操作码，确定它是ARP回复信息
        byte[] opcode = new byte[2];
        System.arraycopy(data, ARP_OPCODE_START, opcode, 0, 2);
        //转换为小端字节序
        short op = ByteBuffer.wrap(opcode).getShort();
        if (op != ARPPacket.ARP_REPLY)
            return false;

        //获取接收者IP，确定该数据包是回复给我们的
        byte[] ip = dataLink.deviceIPAddress();
        for(int i = 0; i < 4; i++){
            if (ip[i] != data[ARP_TARGET_IP_START + i])
                return false;
        }

        //获取发送者IP
        byte[] senderIP = new byte[4];
        System.arraycopy(data, ARP_SENDER_IP_START, senderIP, 0, 4);
        //获取发送者mac地址
        byte[] senderMac = new byte[6];
        System.arraycopy(data, ARP_SENDER_MAC_START, senderMac, 0, 6);

        //更新arp缓存表
        ipToMacTable.put(senderIP, senderMac);

        //通知接收者mac地址
        int ipToInteger = ByteBuffer.wrap(senderIP).getInt();
        ArrayList<IMacReceiver> receivers = ipToMacReceiverTable.get(ipToInteger);
        if(receivers != null){
            for(IMacReceiver receiver : receivers){
                receiver.receiveMacAddress(senderIP, senderMac);
            }
        }
        return true;
    }

    private void sendARPRequestMsg(byte[] ip) {
        if(ip == null)
            return;

        byte[] broadcast = new byte[]{(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255};
        int pointer = 0;

        byte[] data = new byte[28];
        data[pointer] = 0;
        pointer++;
        data[pointer] = 1;
        pointer++;
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putShort(ARPPacket.PROTOTYPE_IP);
        for(int i = 0; i < buffer.array().length; i++){
            data[pointer] = buffer.array()[i];
            pointer++;
        }

        data[pointer] = 6;
        pointer++;
        data[pointer] = 4;
        pointer++;

        buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putShort(ARPPacket.ARP_REQUEST);
        for(int i = 0; i < buffer.array().length; i++){
            data[pointer] = buffer.array()[i];
            pointer++;
        }

        byte[] macAddress = dataLink.deviceMacAddress();
        for(int i = 0; i < macAddress.length; i++){
            data[pointer] = macAddress[i];
            pointer++;
        }

        byte[] sourceIP = dataLink.deviceIPAddress();
        for(int i = 0; i < sourceIP.length; i++){
            data[pointer] = sourceIP[i];
            pointer++;
        }

        for(int i = 0; i < broadcast.length; i++){
            data[pointer] = broadcast[i];
            pointer++;
        }

        for(int i = 0; i < ip.length; i++){
            data[pointer] = ip[i];
            pointer++;
        }

        dataLink.sendData(data, broadcast, EthernetPacket.ETHERTYPE_ARP);
    }

}
