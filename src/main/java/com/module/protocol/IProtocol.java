package com.module.protocol;

import com.module.protocol.datalink.DataLinkLayer;
import jpcap.packet.Packet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * 详解TCP数据包中SYN，ACK字段与数据发送的关联
 * https://cloud.tencent.com/developer/article/1514256
 * 从0到1用java再造tcpIp协议栈
 * https://www.jianshu.com/p/c00b4e1a38cc
 */
@Component
public interface IProtocol {
    byte[] createHeader(HashMap<String, Object> headerInfo);
    HashMap<String, Object> handlePacket(Packet packet);
}
