package com.module.protocol.ping;

import jpcap.packet.Packet;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Random;

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

        byte[] buffer = new byte[ICMP_ECHO_HEADER_LENGTH];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

        short type = ICMP_ECHO_TYPE;
        if(headerName == "echo_reply")
            type = ICMP_ECHO_REPLY_TYPE;

        byteBuffer.putShort(type);
        short code = 0;
        byteBuffer.putShort(code);

        short checkSum = 0;
        byteBuffer.putShort(checkSum);

        short identifier = 0;
        if(headerInfo.get("identifier") == null) {
            Random random = new Random();
            identifier = (short)random.nextInt();
            headerInfo.put("identifier", identifier);
        }
        identifier = (short)headerInfo.get("identifier");
        byteBuffer.putShort(identifier);

        short sequenceNumber = 0;
        if (headerInfo.get("sequence_number") != null) {
            sequenceNumber = (short) headerInfo.get("sequence_number");
            sequenceNumber += 1;
        }
        headerInfo.put("sequence_number", sequenceNumber);
        byteBuffer.putShort(sequenceNumber);

        checkSum = 0;
        byteBuffer.putShort(4, checkSum);

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
             header.put("data", packet.data);

         return header;
    }
}
