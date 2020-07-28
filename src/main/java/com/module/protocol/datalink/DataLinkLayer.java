package com.module.protocol.datalink;

import jpcap.*;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Inet4Address;

/**
 * 数据链路层
 */
@Component
public class DataLinkLayer implements PacketReceiver {
    @Autowired ApplicationContext appContext;

    private NetworkInterface device;
    private Inet4Address ipAddress;
    private byte[] macAddress;
    JpcapSender sender;


    public void initWithOpenDevice(NetworkInterface device){
        this.device = device;
        ipAddress = getDeviceIpAddress();
        macAddress = new byte[6];
        getDeviceMacAddress();

        JpcapCaptor captor = null;
        try{
            captor = JpcapCaptor.openDevice(device,2000,false,3000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sender = captor.getJpcapSenderInstance();

    }

    public byte[] deviceIPAddress() {
        return ipAddress.getAddress();
    }

    public byte[] deviceMacAddress() {
        return macAddress;
    }


    public void sendData(byte[] data, byte[] dstMacAddress, short frameType){
        /**
         * 给上层协议要发送的数据添加数据链路层包头，然后使用网卡发送出去
         */
        if (data == null)
            return;

        Packet packet = new Packet();
        packet.data = data;

        /**
         * 数据链路层会给发送数据添加包头：
         * 0-5字节：接受者的mac地址
         * 6-11字节： 发送者mac地址
         * 12-13字节：数据包发送类型，0x0806表示ARP包，0x0800表示ip包，
         */
        EthernetPacket ether = new EthernetPacket();
        ether.frametype = frameType;
        ether.src_mac = this.device.mac_address;
        ether.dst_mac = dstMacAddress;
        packet.datalink = ether;

        sender.sendPacket(packet);
    }


    @Override
    public void receivePacket(Packet packet) {
        appContext.publishEvent(new DataReceiveEvent("DataLinkLayer").withDataPacket(packet));
    }

    private Inet4Address getDeviceIpAddress() {
        for(NetworkInterfaceAddress temp : device.addresses){
            if(!(temp.address instanceof Inet4Address))
                continue;
            return (Inet4Address) temp.address;
        }
        return null;
    }

    private void getDeviceMacAddress() {
        int count = 0;
        for(byte b : device.mac_address){
            macAddress[count] = (byte)(b & 0xff);
            count++;
        }
    }

}
