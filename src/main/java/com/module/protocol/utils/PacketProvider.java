package com.module.protocol.utils;

import jpcap.PacketReceiver;
import jpcap.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class PacketProvider implements IPacketProvider {
    private List<PacketReceiver> receiverList = new ArrayList<>();

    @Override
    public void registerPacketReceiver(PacketReceiver receiver) {
        if(receiverList.contains(receiver) != true)
            receiverList.add(receiver);
    }

    protected void pushPacketToReceivers(Packet packet) {
        for(int i = 0; i < receiverList.size(); i++) {
            PacketReceiver receiver = receiverList.get(i);
            receiver.receivePacket(packet);
        }
    }
}
