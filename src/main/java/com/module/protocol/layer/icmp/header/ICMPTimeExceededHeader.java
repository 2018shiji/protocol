package com.module.protocol.layer.icmp.header;

import com.module.protocol.layer.IProtocol;
import jpcap.packet.Packet;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.HashMap;

@Component
public class ICMPTimeExceededHeader implements IProtocol {
    private static byte ICMP_TIME_EXCEEDED_TYPE = 1;
    private static byte ICMP_TIME_EXCEEDED_CODE = 0;
    private static int ICMP_TIME_EXCEEDED_DATA_OFFSET = 8;

    @Override
    public byte[] createHeader(HashMap<String, Object> headerInfo) {
        return new byte[0];
    }

    @Override
    public HashMap<String, Object> handlePacket(Packet packet) {
        ByteBuffer buffer = ByteBuffer.wrap(packet.header);
        if(buffer.get(0) != ICMP_TIME_EXCEEDED_TYPE && buffer.get(1) != ICMP_TIME_EXCEEDED_CODE)
            return null;

        HashMap<String, Object> headerInfo = new HashMap<>();
        headerInfo.put("type", ICMP_TIME_EXCEEDED_TYPE);
        headerInfo.put("code", ICMP_TIME_EXCEEDED_CODE);

        byte[] data = new byte[packet.header.length - ICMP_TIME_EXCEEDED_DATA_OFFSET];
        buffer.position(ICMP_TIME_EXCEEDED_DATA_OFFSET);
        buffer.get(data, 0, data.length);
        headerInfo.put("icmp_time_exceed_data", data);
        return headerInfo;

    }
}
