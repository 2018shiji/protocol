package com.module.protocol.icmp.header;

import com.module.protocol.IProtocol;
import jpcap.packet.Packet;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ICMPTimeStampHeader implements IProtocol {
    @Override
    public byte[] createHeader(HashMap<String, Object> headerInfo) {
        return new byte[0];
    }

    @Override
    public HashMap<String, Object> handlePacket(Packet packet) {
        return null;
    }
}
