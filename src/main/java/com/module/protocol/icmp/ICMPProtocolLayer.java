package com.module.protocol.icmp;

import com.module.protocol.IProtocol;
import jpcap.packet.Packet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 解读或者构造ICMP数据包
 */
@Component("icmpProtocolLayer")
public class ICMPProtocolLayer implements IProtocol {

    public static byte PROTOCOL_ICMP = 1;
    private List<IProtocol> protocol_header_list = new ArrayList<>();
    private Packet packet;

    public ICMPProtocolLayer() {
        //增加icmp echo 协议包头创建对象
        protocol_header_list.add(new ICMPEchoHeader());
        protocol_header_list.add(new ICMPTimeExceededHeader());
    }


    @Override
    public byte[] createHeader(HashMap<String, Object> headerInfo) {
        for(int i = 0; i < protocol_header_list.size(); i++){
            byte[] buff = protocol_header_list.get(i).createHeader(headerInfo);
            if(buff != null) return buff;
        }
        return null;
    }

    @Override
    public HashMap<String, Object> handlePacket(Packet packet) {
        this.packet = packet;
        return analyzeICMPMessage();
    }

    private HashMap<String, Object> analyzeICMPMessage() {
        HashMap<String, Object> info = null;
        for(int i = 0; i < protocol_header_list.size(); i++) {
            IProtocol handler = protocol_header_list.get(i);
            info = handler.handlePacket(packet);
        }

        return info;
    }

}
