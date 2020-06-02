package com.module.protocol.icmp;

import jpcap.packet.IPPacket;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

/**
 * 解读ICMP错误数据报
 */
@Component
public class ICMPErrorMsgHandler implements IICMPErrorMsgHandler {
    private static int ICMP_UNREACHABLE_TYPE = 3;
    private static int IP_HEADER_LENGTH = 20;

    enum ICMP_ERROR_MSG_CODE {
        ICMP_NETWORK_UNREACHABLE,
        ICMP_HOST_UNREACHABLE,
        ICMP_PROTOCOL_UNREACHABLE,
        ICMP_PORT_UNREACHABLE
    };


    @Override
    public boolean handlerICMPErrorMsgHandler(int type, int code, byte[] data) {
        if(type != ICMP_UNREACHABLE_TYPE)
            return false;

        ByteBuffer buffer = ByteBuffer.wrap(data);

        switch(ICMP_ERROR_MSG_CODE.values()[code]){
            case ICMP_PORT_UNREACHABLE:
                //错误数据格式：IP包头+8字节内容
                //获取协议类型
                byte protocol = buffer.get(9);
                if(protocol == IPPacket.IPPROTO_UDP)
                    handleUDPError(buffer);

        }
        return false;
    }

    private void handleUDPError(ByteBuffer buffer) {
        System.out.println("protocol of error packet is UDP");
        System.out.println("Source IP Address is: ");
        int source_ip_offset = 12;
        for(int i = 0; i < 4; i++){
            int v = buffer.get(source_ip_offset + i) & 0xff;
            System.out.println(v + ".");
        }

        System.out.println("\nDestination IP Address is:");
        int dest_ip_offset = 16;
        for(int i = 0; i < 4; i++) {
            int v = buffer.get(dest_ip_offset + i) & 0xff;
            System.out.println(v + ".");
        }

        /**
         * 打印UDP数据包头前8个字节信息，其格式为：
         * source_port(2 byte)
         * dest_port(2 byte)
         * ...
         */
        int source_port = buffer.getShort(IP_HEADER_LENGTH) & 0xFFFF;
        System.out.println("\nSource Port: " + source_port);
        int source_port_len = 2;
        int dest_port = buffer.getShort(IP_HEADER_LENGTH + source_port_len) & 0xFFFF;
        System.out.println("\nDestination Port: " + dest_port);
    }
}
