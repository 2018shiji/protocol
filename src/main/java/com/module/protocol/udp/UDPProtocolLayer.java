package com.module.protocol.udp;

import com.module.protocol.IProtocol;
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

        //UDP包头的checksum可以直接设置成0xFFFF
        char checksum = 65535;
        byteBuffer.putChar(checksum);

        if(data != null) byteBuffer.put(data);


        return byteBuffer.array();
    }

    @Override
    public HashMap<String, Object> handlePacket(Packet packet) {
        System.out.println("UDPProtocolLayer receive packet at: "
                + new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime()));
        return null;
    }

}
