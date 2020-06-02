package com.module.protocol.ping;

import com.module.protocol.datalink.DataLinkLayer;
import jpcap.packet.Packet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.HashMap;

@Component("ipProtocolLayer")
public class IPProtocolLayer implements IProtocol {
    private static int ETHERNET_FRAME_HEADER_LENGTH = 14;
    private static byte IP_VERSION = 4;
    private static int CHECKSUM_OFFSET = 10;
    private static int HEADER_LENGTH_OFFSET = 0 + ETHERNET_FRAME_HEADER_LENGTH;
    private static int TOTAL_LENGTH_OFFSET = 2 + ETHERNET_FRAME_HEADER_LENGTH;
    private static int PROTOCOL_INDICATOR_OFFSET = 9 + ETHERNET_FRAME_HEADER_LENGTH;
    private static int SOURCE_IP_OFFSET = 12 + ETHERNET_FRAME_HEADER_LENGTH;
    private static int DEST_IP_OFFSET = 16 + ETHERNET_FRAME_HEADER_LENGTH;

    @Autowired
    private DataLinkLayer dataLink;

    @Override
    public byte[] createHeader(HashMap<String, Object> headerInfo) {
        byte version = IP_VERSION;
        byte internetHeaderLength = 5;
        if(headerInfo.get("internet_header_length") != null)
            internetHeaderLength = (byte)headerInfo.get("internet_header_length");

        byte[] buffer = new byte[internetHeaderLength];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.put((byte) (internetHeaderLength << 4 | version));

        byte dscp = 0;
        if (headerInfo.get("dscp") != null) {
            dscp = (byte)headerInfo.get("dscp");
        }
        byte ecn = 0;
        if (headerInfo.get("ecn") != null) {
            ecn = (byte)headerInfo.get("ecn");
        }
        byteBuffer.put((byte)(dscp | ecn << 6));

        if (headerInfo.get("total_length") == null) {
            return null;
        }

        short totalLength = (short)headerInfo.get("total_length");
        byteBuffer.putShort(totalLength);
        int identification = 0;
        if (headerInfo.get("identification") != null) {
            identification = (int)headerInfo.get("identification");
        }
        byteBuffer.putInt(identification);

        short flagAndOffset = 0;
        if (headerInfo.get("flag") != null) {
            flagAndOffset = (short)headerInfo.get("flag");
        }
        if (headerInfo.get("fragment_offset") != null) {
            flagAndOffset |= ((short)headerInfo.get("fragment_offset")) << 3;
        }
        byteBuffer.putShort(flagAndOffset);

        short timeToLive = 64;
        if (headerInfo.get("time_to_live") != null) {
            timeToLive = (short)headerInfo.get("time_to_live");
        }
        byteBuffer.putShort(timeToLive);

        short protocol = 0;
        if (headerInfo.get("protocol") == null) {
            return null;
        }
        protocol = (short)headerInfo.get("protocol");
        byteBuffer.putShort(protocol);

        short checkSum = 0;
        byteBuffer.putShort(checkSum);

        int srcIP = 0;
        if (headerInfo.get("source_ip") == null) {
            return null;
        }
        srcIP = (int)headerInfo.get("source_ip");
        byteBuffer.putInt(srcIP);

        int destIP = 0;
        if (headerInfo.get("destination_ip") == null) {
            return null;
        }
        byteBuffer.putInt(destIP);

        if (headerInfo.get("options") != null) {
            byte[] options = (byte[]) headerInfo.get("options");
            byteBuffer.put(options);
        }

        checkSum = 0;
        byteBuffer.putShort(CHECKSUM_OFFSET, checkSum);

        return byteBuffer.array();

    }

    public HashMap<String, Object> handlePacket(Packet packet) {
        /**
         * 解析收到数据包的IP包头，暂时不做校验和检测，
         * 默认网络发送的数据包不会出错，暂时忽略对option段的处理
         */
        byte[] ip_data = new byte[packet.header.length + packet.data.length];
        ByteBuffer buffer = ByteBuffer.wrap(ip_data);
        buffer.put(packet.header);
        buffer.put(packet.data);

        HashMap<String, Object> headerInfo = new HashMap<>();

        //获取发送者ip
        byte[] src_ip = new byte[4];
        buffer.position(SOURCE_IP_OFFSET);
        buffer.get(src_ip, 0, 4);
        headerInfo.put("source_ip", src_ip);

        //获取接收者IP
        byte[] dest_ip = new byte[4];
        buffer.position(DEST_IP_OFFSET);
        buffer.get(dest_ip, 0, 4);
        headerInfo.put("dest_ip", dest_ip);

        //确保接收者是我们自己
        byte[] ip = dataLink.deviceIPAddress();
        for(int i = 0; i < ip.length; i++){
            if(ip[i] != dest_ip[i])
                return null;
        }

        //获得下一层协议编号
        buffer.position(0);
        byte protocol = buffer.get(PROTOCOL_INDICATOR_OFFSET);
        headerInfo.put("protocol", protocol);

        int k = 0;
        if(protocol == 1){
            k = 2;
            System.out.println("receive protocol 2");
        }

        byte headerLength = buffer.get(HEADER_LENGTH_OFFSET);
        headerLength &= 0x0F;
        //*4得到包头字节长度
        headerLength *= 4;
        short totalLength = buffer.getShort(TOTAL_LENGTH_OFFSET);
        int dataLength = totalLength - headerLength;
        byte[] data = new byte[dataLength];
        buffer.position(headerLength + ETHERNET_FRAME_HEADER_LENGTH);
        buffer.get(data, 0, dataLength);
        headerInfo.put("header", data);

        return headerInfo;
    }



}
