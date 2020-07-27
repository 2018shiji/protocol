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

    /**https://www.cnblogs.com/muyuchengguang/p/9854886.html **/
    public static String bytesToHex(byte[] bytes){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++){
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2)
                sb.append(0);
            sb.append(hex);
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String hexString){
        int hexLen = hexString.length();
        byte[] result;
        if(hexLen % 2 == 1){
            hexLen++;
            result = new byte[hexLen/2];
            hexString = "0" + hexString;
        } else {
            result = new byte[hexLen/2];
        }
        int j = 0;
        for(int i = 0; i < hexLen; i += 2){
            result[j] = (byte)Integer.parseInt(hexString.substring(i, i+2), 16);
        }
        return result;
    }
}
