package com.module.protocol.layer.icmp.header;

import com.module.protocol.layer.IProtocol;
import com.module.protocol.utils.NumUtility;
import jpcap.packet.Packet;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

/**
 * 构造协议头
 */
@Component
public class ICMPEchoHeader implements IProtocol {
    private static int ICMP_ECHO_HEADER_LENGTH = 8;
    private static byte ICMP_ECHO_TYPE = 8;
    private static byte ICMP_ECHO_REPLY_TYPE = 0;
    private static short ICMP_ECHO_IDENTIFIER_OFFSET = 4;
    private static short ICMP_ECHO_SEQUENCE_NUM_OFFSET = 6;
    private static short ICMP_ECHO_OPTIONAL_DATA_OFFSET = 8;
    private static short ICMP_ECHO_ONLY_HEADER_LENGTH = 8;


    @Override
    public byte[] createHeader(HashMap<String, Object> headerInfo) {
        String headerName = (String)headerInfo.get("header");
        if(headerName != "echo" && headerName != "echo_reply")
            return null;

        int bufferLen = ICMP_ECHO_HEADER_LENGTH;

        if(headerInfo.get("data") != null){
            bufferLen += ((byte[])headerInfo.get("data")).length;
        }

        byte[] buffer = new byte[bufferLen];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

        byte type = ICMP_ECHO_TYPE;
        if(headerName == "echo_reply")
            type = ICMP_ECHO_REPLY_TYPE;

        byteBuffer.put(type);
        byte code = 0;
        byteBuffer.put(code);

        short checkSum = 0;
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putShort(checkSum);

        short identifier = 0;
        if(headerInfo.get("identifier") == null) {
//            Random random = new Random();
//            identifier = (short)random.nextInt();
            identifier = 50;
            headerInfo.put("identifier", identifier);
        }

        identifier = (short)headerInfo.get("identifier");
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putShort(identifier);

        short sequenceNumber = 0;
        if (headerInfo.get("sequence_number") != null) {
            sequenceNumber = (short) headerInfo.get("sequence_number");
        }
        headerInfo.put("sequence_number", sequenceNumber);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putShort(sequenceNumber);

        if (headerInfo.get("data") != null) {
            byte[] data = (byte[])headerInfo.get("data");

            byteBuffer.put(data, 0, data.length);
        }

        checkSum = (short) NumUtility.checksum(byteBuffer.array(), byteBuffer.array().length);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putShort(2, checkSum);

        return byteBuffer.array();
    }

    @Override
    public HashMap<String, Object> handlePacket(Packet packet) {
        ByteBuffer buffer = ByteBuffer.wrap(packet.header);
        if(buffer.get(0) != ICMP_ECHO_REPLY_TYPE)
            return null;

         HashMap<String, Object> header = new HashMap<>();
         header.put("identifier", buffer.getShort(ICMP_ECHO_IDENTIFIER_OFFSET));
         header.put("sequence", buffer.getShort(ICMP_ECHO_SEQUENCE_NUM_OFFSET));
         if(packet.header.length > ICMP_ECHO_ONLY_HEADER_LENGTH)
             header.put("icmp_echo_data", packet.data);

         return header;
    }
}
