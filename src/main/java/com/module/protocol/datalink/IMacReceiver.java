package com.module.protocol.datalink;

public interface IMacReceiver {
    void receiveMacAddress(byte[] ip, byte[] mac);
}
