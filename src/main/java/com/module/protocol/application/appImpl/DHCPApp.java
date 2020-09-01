package com.module.protocol.application.appImpl;

import com.module.protocol.layer.IProtocol;
import com.module.protocol.ProtocolManager;
import com.module.protocol.application.AppDataEvent;
import com.module.protocol.application.Application;
import com.module.protocol.application.ApplicationGroup;
import com.module.protocol.layer.datalink.DataLinkLayer;
import com.module.protocol.layer.udp.UDPProtocolLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class DHCPApp extends Application {
    @Autowired private ProtocolManager protocolManager;
    @Autowired private DataLinkLayer dataLinkLayer;
    @Autowired ApplicationGroup appGroup;

    private static byte[] dhcp_front_part;
    private static int DHCP_FRONT_PART_LENGTH = 236;
    private static byte MESSAGE_TYPE_REQUEST = 1;
    private static byte HARDWARE_TYPE = 1;
    private static byte HARDWARE_ADDR_LENGTH = 6;
    private static byte DHCP_HOPS = 0;

    private int transaction_id = 0;
    private short secs_elapsed = 0;
    private short bootp_flags = 0;

    private byte[] client_ip_address = new byte[4];
    private byte[] your_ip_address = new byte[4];
    private byte[] next_server_ip_address = new byte[4];
    private byte[] relay_agent_ip_address = new byte[4];

    private static byte[] MAGIC_COOKIE = new byte[] {0x63, (byte)0x82, 0x53, 0x63};
    private static byte[] dhcp_options_part;

    private static byte OPTION_MSG_TYPE_LENGTH = 3;
    private static byte OPTION_MSG_TYPE = 53;
    private static byte OPTION_MSG_LENGTH = 1;
    private static byte OPTION_MSG_REQUEST = 3;

    private static byte OPTION_CLIENT_IDENTIFIER_LENGTH = 9;
    private static byte OPTION_CLIENT_IDENTIFIER = 61;
    private static byte OPTION_CLIENT_IDENTIFIER_DATA_LENGTH = 7;
    private static byte OPTION_CLIENT_IDENTIFIED_HARDWARE_TYPE = 0X01;

    private static byte OPTION_CLIENT_IP_ADDRESS_LENGTH = 6;
    private static byte OPTION_CLIENT_IP_ADDRESS = 50;
    private static byte OPTION_CLIENT_IP_ADDRESS_DATA_LENGTH = 4;


    private static byte OPTION_CLIENT_HOST_NAME = 12;
    private static byte[] OPTION_HOST_NAME_CONTENT = "DESKTOP-V18O007".getBytes();
    private static byte OPTION_HOST_NAME_DATA_LENGTH = (byte)OPTION_HOST_NAME_CONTENT.length;
    private static int OPTION_HOST_LENGTH = 2 + OPTION_HOST_NAME_CONTENT.length;

    private static byte OPTION_CLIENT_HOST_FULL_NAME = 81;
    private static byte[] OPTION_HOST_FULL_NAME_CONTENT = "DESKTOP-V18O007.szcmml.com".getBytes();
    private static byte OPTION_HOST_FULL_NAME_DATA_LENGTH = (byte)(3 + OPTION_HOST_FULL_NAME_CONTENT.length);
    private static int OPTION_HOST_FULL_LENGTH = 2 + OPTION_HOST_FULL_NAME_DATA_LENGTH;


    private static byte OPTION_VENDOR_CLASS_IDENTIFIER = 60;
    private static byte[] OPTION_VENDOR_CLASS_CONTENT = "MSFT 5.0".getBytes();
    private static byte OPTION_VENDOR_CLASS_DATA_LENGTH = (byte)OPTION_VENDOR_CLASS_CONTENT.length;
    private static int OPTION_VENDOR_CLASS_LENGTH = 2 + OPTION_VENDOR_CLASS_CONTENT.length;

    private static byte OPTION_PARAM_REQUEST_LIST = 55;
    private static byte[] OPTION_PARAM_REQUEST_LIST_CONTENT = {0x01, 0x03, 0x06, 0x0f, 0x1f, 0x21, 0x2b, 0x2c, 0x2e, 0x2f, 0x77, 0x79, (byte)0xf9, (byte)0xfc};
    private static byte OPTION_PARAM_DATA_LENGTH = (byte) OPTION_PARAM_REQUEST_LIST_CONTENT.length;
    private static int OPTION_PARAM_REQUEST_LIST_LENGTH = 2 + OPTION_PARAM_REQUEST_LIST_CONTENT.length;

    private static byte OPTION_END = (byte)0xff;

    private static char srcPort =68;
    private static char dstPort = 67;

    private static short DHCP_MSG_REPLY = 2;
    private static short DHCP_MSG_TYPE_OFFSET = 0;
    private static short DHCP_YOUR_IP_ADDRESS_OFFSET = 16;
    private static short DHCP_NEXT_IP_ADDRESS_OFFSET = 20;
    private static short DHCP_OPTIONS_OFFSET = 240;

    private static final byte DHCP_MSG_TYPE = 53;
    private static final byte DHCP_SERVER_IDENTIFIER = 54;
    private static final byte DHCP_IP_ADDRESS_LEASE_TIME = 51;
    private static final byte DHCP_RENEWAL_TIME = 58;
    private static final byte DHCP_REBINDING_TIME = 59;
    private static final byte DHCP_SUBNET_MASK = 1;
    private static final byte DHCP_BROADCAST_ADDRESS = 28;
    private static final byte DHCP_ROUTER = 3;
    private static final byte DHCP_DOMAIN_NAME_SERVER = 6;
    private static final byte DHCP_DOMAIN_NAME = 15;

    private static final int DHCP_STATE_DISCOVER = 0;
    private static final byte DHCP_MSG_OFFER = 2;
    private static final int DHCP_STATE_REQUESTING = 1;
    private static final byte DHCP_MSG_ACK =5;

    private static int dhcp_current_state = DHCP_STATE_DISCOVER;


    @PostConstruct
    public void initDHCPApp(){
        Random rand = new Random();
        transaction_id = rand.nextInt();
        appGroup.registerToUDPAppList(this);
    }

    private void constructDHCPFrontPart(){
        dhcp_front_part = new byte[DHCP_FRONT_PART_LENGTH];
        ByteBuffer buffer = ByteBuffer.wrap(dhcp_front_part);
        //设置数据包类型
        buffer.put(MESSAGE_TYPE_REQUEST);
        //设置网络类型
        buffer.put(HARDWARE_TYPE);
        //设置硬件地址长度
        buffer.put(HARDWARE_ADDR_LENGTH);
        //设置数据包跳转次数
        buffer.put(DHCP_HOPS);

        //设置会话id
        buffer.putInt(transaction_id);
        //设置等待时间
        buffer.putShort(secs_elapsed);
        //设置标志位
        buffer.putShort(bootp_flags);
        //设置设备ip
        buffer.put(client_ip_address);
        //设置租借ip
        buffer.put(your_ip_address);
        //设置下一个服务器ip
        buffer.put(next_server_ip_address);
        //设置网关ip
        buffer.put(relay_agent_ip_address);
        //设置硬件地址
        buffer.put(dataLinkLayer.deviceMacAddress());
        //填充接下来的10个字节
        byte[] padding = new byte[10];
        buffer.put(padding);
        //设置64字节的服务器名称
        byte[] host_name = new byte[64];
        buffer.put(host_name);
        //设置128位的byte字段
        byte[] file = new byte[128];
        buffer.put(file);
    }

    private void constructDHCPRequestOptionsPart(){
        //option 53 DHCP Message Type
        byte[] option_msg_type = new byte[OPTION_MSG_TYPE_LENGTH];
        ByteBuffer buffer = ByteBuffer.wrap(option_msg_type);
        buffer.put(OPTION_MSG_TYPE);
        buffer.put(OPTION_MSG_LENGTH);
        buffer.put(OPTION_MSG_REQUEST);

        //option 61 Client identifier
        byte[] client_identifier = new byte[OPTION_CLIENT_IDENTIFIER_LENGTH];
        buffer = ByteBuffer.wrap(client_identifier);
        buffer.put(OPTION_CLIENT_IDENTIFIER);
        buffer.put(OPTION_CLIENT_IDENTIFIER_DATA_LENGTH);
        buffer.put(OPTION_CLIENT_IDENTIFIED_HARDWARE_TYPE);
        buffer.put(dataLinkLayer.deviceMacAddress());

        //option 50 requested IP Address
        byte[] request_IP_Address = new byte[OPTION_CLIENT_IP_ADDRESS_LENGTH];
        buffer = ByteBuffer.wrap(request_IP_Address);
        buffer.put(OPTION_CLIENT_IP_ADDRESS);
        buffer.put(OPTION_CLIENT_IP_ADDRESS_DATA_LENGTH);
        buffer.put(dataLinkLayer.deviceIPAddress());

        //option 12 Host Name
        byte[] host_name = new byte[OPTION_HOST_LENGTH];
        buffer = ByteBuffer.wrap(host_name);
        buffer.put(OPTION_CLIENT_HOST_NAME);
        buffer.put(OPTION_HOST_NAME_DATA_LENGTH);
        buffer.put(OPTION_HOST_NAME_CONTENT);

        //option 81 host full name
        byte[] host_full_name = new byte[OPTION_HOST_FULL_LENGTH];
        buffer = ByteBuffer.wrap(host_full_name);
        buffer.put(OPTION_CLIENT_HOST_FULL_NAME);
        buffer.put(OPTION_HOST_FULL_NAME_DATA_LENGTH);
        buffer.put((byte)0x00);
        buffer.put((byte)0x00);
        buffer.put((byte)0x00);
        buffer.put(OPTION_HOST_FULL_NAME_CONTENT);

        //option 60 class identifier
        byte[] vendor_class = new byte[OPTION_VENDOR_CLASS_LENGTH];
        buffer = ByteBuffer.wrap(vendor_class);
        buffer.put(OPTION_VENDOR_CLASS_IDENTIFIER);
        buffer.put(OPTION_VENDOR_CLASS_DATA_LENGTH);
        buffer.put(OPTION_VENDOR_CLASS_CONTENT);

        //option 55 parameter request list
        byte[] parameter_list = new byte[OPTION_PARAM_REQUEST_LIST_LENGTH];
        buffer = ByteBuffer.wrap(parameter_list);
        buffer.put(OPTION_PARAM_REQUEST_LIST);
        buffer.put(OPTION_PARAM_DATA_LENGTH);
        buffer.put(OPTION_PARAM_REQUEST_LIST_CONTENT);

        //option end
        byte[] end = new byte[1];
        end[0] = OPTION_END;

        dhcp_options_part = new byte[
                option_msg_type.length + client_identifier.length + request_IP_Address.length +
                        host_name.length + host_full_name.length + vendor_class.length +
                        parameter_list.length + end.length];
        buffer = ByteBuffer.wrap(dhcp_options_part);
        buffer.put(option_msg_type);
        buffer.put(client_identifier);
        buffer.put(request_IP_Address);
        buffer.put(host_name);
        buffer.put(host_full_name);
        buffer.put(vendor_class);
        buffer.put(parameter_list);
        buffer.put(end);

    }

    private byte[] createUDPHeader(byte[] data){
        IProtocol udpProto = protocolManager.getProtocol("udp");
        if(udpProto == null)
            return null;

        HashMap<String, Object> headerInfo = new HashMap<>();
        headerInfo.put("source_port", srcPort);
        headerInfo.put("dest_port", dstPort);

        headerInfo.put("data", data);

        return udpProto.createHeader(headerInfo);
    }

    private byte[] createIP4Header(int dataLength){
        IProtocol ip4Proto = protocolManager.getProtocol("ip");
        if(ip4Proto == null || dataLength <= 0)
            return null;
        //创建IP包头默认情况下只需要发送数据长度，下层协议号，接收方ip地址
        HashMap<String, Object> headerInfo = new HashMap();
        headerInfo.put("data_length", dataLength);
        byte[] broadcastIP = new byte[]{(byte)255, (byte)255, (byte)255, (byte)255};
        byte[] sourceIP = new byte[]{0,0,0,0};

        ByteBuffer srcIP = ByteBuffer.wrap(sourceIP);
        headerInfo.put("source_ip", srcIP.getInt());

        ByteBuffer destIP = ByteBuffer.wrap(broadcastIP);
        headerInfo.put("destination_ip", destIP.getInt());

        byte protocol = UDPProtocolLayer.PROTOCOL_UDP;
        headerInfo.put("protocol", protocol);
        headerInfo.put("identification", (short)srcPort);
        byte[] ipHeader = ip4Proto.createHeader(headerInfo);

        return ipHeader;
    }

    @Override
    public void handleData(AppDataEvent appDataEvent) {
        Map<String, Object> headerInfo = appDataEvent.getHeaderInfo();
        byte[] data = (byte[])headerInfo.get("data");
        boolean readSuccess = readFrontPart(data);
        if(readSuccess)readOptions(data);
    }

    private boolean readFrontPart(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte reply = buffer.get(DHCP_MSG_TYPE_OFFSET);
        if(reply != DHCP_MSG_REPLY)
            return false;

        byte[] your_addr = new byte[4];
        buffer.position(DHCP_YOUR_IP_ADDRESS_OFFSET);
        buffer.get(your_addr, 0, your_addr.length);
        System.out.println("available ip offer by dhcp server is:");
        try{
            InetAddress addr = InetAddress.getByAddress(your_addr);
            System.out.println(addr.getHostAddress());
        } catch (UnknownHostException e){e.printStackTrace();}

        buffer.position(DHCP_NEXT_IP_ADDRESS_OFFSET);
        byte[] next_server_addr = new byte[4];
        buffer.get(next_server_addr, 0, next_server_addr.length);
        try{
            InetAddress addr = InetAddress.getByAddress(next_server_addr);
            System.out.println(addr.getHostAddress());
        } catch (UnknownHostException e){e.printStackTrace();}

        return true;
    }

    private void readOptions(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(DHCP_OPTIONS_OFFSET);
        while(true){
            byte type = buffer.get();
            if(type == OPTION_END)
                break;

            switch(type){
                case DHCP_MSG_TYPE:
                    buffer.get();//越过长度字段
                    if(buffer.get() == DHCP_MSG_OFFER){
                        System.out.println("receive DHCP offer message from server");
                        //接收到DHCP_OFFER后，将状态转变为requesting
                        dhcp_current_state = DHCP_STATE_REQUESTING;
                    }
                    break;
                case DHCP_SERVER_IDENTIFIER:
                    printOptionArray("DHCP server identifier:", buffer);
                    break;
                case DHCP_IP_ADDRESS_LEASE_TIME:
                    buffer.get();//越过长度字段
                    int lease_time_secs = buffer.getInt();
                    System.out.println("The ip will lease to us for " + lease_time_secs + " seconds");
                    break;
                case DHCP_RENEWAL_TIME:
                    buffer.get();//越过长度字段
                    int renew_time = buffer.getInt();
                    System.out.println("we need to renew ip after " + renew_time + " seconds");
                    break;
                case DHCP_REBINDING_TIME:
                    buffer.get();//越过长度字段
                    int rebinding_time = buffer.getInt();
                    System.out.println("we need to rebinding" + rebinding_time + " seconds");
                    break;
                case DHCP_SUBNET_MASK:
                    printOptionArray("Subnet mask is: ", buffer);
                    break;
                case DHCP_BROADCAST_ADDRESS:
                    printOptionArray("Broadcast Address is: ", buffer);
                    break;
                case DHCP_ROUTER:
                    printOptionArray("DHCP router is: ", buffer);
                    break;
                case DHCP_DOMAIN_NAME_SERVER:
                    printOptionArray("Domain name server is: ", buffer);
                    break;
                case DHCP_DOMAIN_NAME:
                    int len = buffer.get();
                    for (int i = 0; i < len; i++) {
                        System.out.println((char)buffer.get() + " ");
                    }
                    break;
            }
        }

        trigger_action_by_state();
    }

    private void printOptionArray(String content, ByteBuffer buffer){
        System.out.println(content);
        int len = buffer.get();
        if(len == 4){
            byte[] buff = new byte[4];
            for(int i = 0; i < len; i++){
                buff[i] = buffer.get();
            }
            try {
                InetAddress addr = InetAddress.getByAddress(buff);
                System.out.println(addr.getHostAddress());
            } catch (UnknownHostException e) { e.printStackTrace(); }
        }else{
            for (int i = 0; i < len; i++) {
                System.out.println(buffer.get() + ".");
            }
        }
        System.out.println("\n");
    }

    private void trigger_action_by_state(){
        switch(dhcp_current_state){
            case DHCP_STATE_REQUESTING:
                dhcpRequest();
                break;
            default:
                break;
        }
    }

    public void dhcpRequest(){
        constructDHCPFrontPart();
        constructDHCPRequestOptionsPart();
        byte[] dhcpDiscoveryBuffer = new byte[dhcp_front_part.length + MAGIC_COOKIE.length + dhcp_options_part.length];
        ByteBuffer buffer = ByteBuffer.wrap(dhcpDiscoveryBuffer);
        buffer.put(dhcp_front_part);
        buffer.put(MAGIC_COOKIE);
        buffer.put(dhcp_options_part);

        byte[] udpHeader = createUDPHeader(dhcpDiscoveryBuffer);
        byte[] ipHeader = createIP4Header(udpHeader.length);

        byte[] dhcpPacket = new byte[ipHeader.length + udpHeader.length];
        buffer = ByteBuffer.wrap(dhcpPacket);
        buffer.put(ipHeader);
        buffer.put(udpHeader);
        //将消息广播出去
        protocolManager.broadcastData(dhcpPacket);
    }
}
