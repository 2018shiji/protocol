package com.module.protocol.datalink;

import jpcap.packet.Packet;
import org.springframework.context.ApplicationEvent;

public class DataReceiveEvent extends ApplicationEvent {
    private Packet packet;

    public DataReceiveEvent(Object source) {
        super(source);
    }

    public DataReceiveEvent withDataPacket(Packet packet){
        this.packet = packet;
        return this;
    }

    public Packet getDataPacket(){
        return packet;
    }
}
