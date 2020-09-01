package com.module.protocol.layer.udp;

import com.module.protocol.layer.IProtocol;
import jpcap.packet.Packet;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

@Component
public class UDPProtocolLayer implements IProtocol {

    private static short UDP_LENGTH_WITHOUT_DATA = 8;
    public static byte PROTOCOL_UDP = 17;

    private static final short UDP_SRC_PORT_OFFSET = 0;
    private static final short UDP_DST_PORT_OFFSET = 2;
    private static final short UDP_LENGTH_OFFSET = 4;

    @Override
    public byte[] createHeader(HashMap<String, Object> headerInfo) {
        if(headerInfo.get("source_port") == null || headerInfo.get("dest_port") == null)
            return null;

        short total_length = UDP_LENGTH_WITHOUT_DATA;
        byte[] data = null;
        if(headerInfo.get("data") != null){
            data = (byte[])headerInfo.get("data");
            total_length += data.length;
        }

        byte[] buf = new byte[total_length];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buf);

        char srcPort = (char)headerInfo.get("source_port");
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putChar(srcPort);

        char destPort = (char)headerInfo.get("dest_port");
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putChar(destPort);

        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putShort(total_length);

        //UDP包头的checksum可以直接设置成0xFFFF, DHCP情况下需设置为0
        char checksum = 0;
//        char checksum = 65535;
        byteBuffer.putChar(checksum);

        if(data != null) byteBuffer.put(data);


        return byteBuffer.array();
    }

    @Override
    public HashMap<String, Object> handlePacket(Packet packet) {
        System.out.println("UDPProtocolLayer receive packet at: "
                + new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime()));
        ByteBuffer buffer = ByteBuffer.wrap(packet.header);
        HashMap<String, Object> headerInfo = new HashMap();

        headerInfo.put("src_port", buffer.getShort(UDP_SRC_PORT_OFFSET));
        headerInfo.put("dest_port", buffer.getShort(UDP_DST_PORT_OFFSET));
        headerInfo.put("length", buffer.getShort(UDP_LENGTH_OFFSET));
        headerInfo.put("data", packet.data);

        return headerInfo;
    }

}
