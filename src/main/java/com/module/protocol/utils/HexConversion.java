package com.module.protocol.utils;

public abstract class HexConversion {
    public static String bytes2Ipv4(byte[] bytesIp){
        String strIp = "";
        for(int i = 0; i < bytesIp.length; i++){
            strIp += (bytesIp[i]&0xFF) + ".";
        }
        return strIp.substring(0, strIp.length()-1);
    }

    public static byte[] ipv42Bytes(String strIp){
        byte[] bytesIp = new byte[4];
        String[] strIps = strIp.split("\\.");
        for(int i = 0; i < strIps.length; i++){
            bytesIp[i] = (byte)Integer.parseInt(strIps[i]);
        }
        return bytesIp;
    }
}
