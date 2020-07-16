package com.module.protocol.datalink;

import com.module.protocol.arp.ARPProtocolLayer;
import com.module.protocol.utils.IMacReceiver;
import com.module.protocol.utils.PacketProvider;
import jpcap.*;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 数据链路层
 */
@Component
public class DataLinkLayer extends PacketProvider implements PacketReceiver, IMacReceiver {
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
        ether.frametype = EthernetPacket.ETHERTYPE_ARP;
        ether.src_mac = this.device.mac_address;
        ether.dst_mac = dstMacAddress;
        packet.datalink = ether;

        sender.sendPacket(packet);
    }

    @Override
    public void receiveMacAddress(byte[] ip, byte[] mac) {
        System.out.println("receive arp reply msg with sender ip:");
        for(byte b : ip){
            System.out.print(Integer.toUnsignedString(b & 0xff) + ".");
        }
        System.out.println();
        System.out.println("with sender mac: ");
        for(byte b : mac){
            System.out.print(Integer.toHexString(b & 0xff) + ":");
        }
        System.out.println();
    }

    @Override
    public void receivePacket(Packet packet) {
        pushPacketToReceivers(packet);
    }

    private Inet4Address getDeviceIpAddress() {
        for(NetworkInterfaceAddress temp : device.addresses){
            System.out.println(temp.address.getHostName());
            //网卡网址符合ipv4规范才是可用网卡
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
