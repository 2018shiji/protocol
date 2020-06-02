package com.module.protocol.utils;

import jpcap.PacketReceiver;
import org.springframework.stereotype.Component;

@Component
public interface IPacketProvider {
    void registerPacketReceiver(PacketReceiver receiver);
}
