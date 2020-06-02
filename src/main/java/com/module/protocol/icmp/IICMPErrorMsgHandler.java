package com.module.protocol.icmp;

import org.springframework.stereotype.Component;

@Component
public interface IICMPErrorMsgHandler {
    boolean handlerICMPErrorMsgHandler(int type, int code, byte[] data);
}
