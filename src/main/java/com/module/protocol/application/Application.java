package com.module.protocol.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.*;

//由ProtocolManager发起事件通知，Application监听事件通知
@Component
public abstract class Application implements ApplicationListener<AppDataEvent> {
    @Autowired ApplicationGroup appGroup;

    @Override
    public void onApplicationEvent(AppDataEvent appDataEvent) {
        int j = 0;
        for(Map.Entry entry : appDataEvent.getHeaderInfo().entrySet()){
            System.out.print(entry.getKey() + ": " + entry.getValue() + "\t\t");
            if(++j%4 == 0) System.out.println();
        }
        switch((String) appDataEvent.getSource()){
            case "ip":
                System.out.println("Application ip protocol receive");
                for(int i = 0; i < appGroup.getIpAppList().size(); i++){
                    appGroup.getIpAppList().get(i).handleData(appDataEvent);
                }
                break;
            case "udp":
                System.out.println("Application udp protocol receive");
                for(int i = 0; i < appGroup.getUdpAppList().size(); i++){
                    appGroup.getUdpAppList().get(i).handleData(appDataEvent);
                }
                break;
            case "icmp":
                System.out.println("Application icmp protocol receive");
                for(int i = 0; i < appGroup.getIcmpAppList().size(); i++){
                    appGroup.getIcmpAppList().get(i).handleData(appDataEvent);
                }
                break;
            default:
                System.out.println("ohhhhhhh emmmmmmm it seems like a new protocol");
                break;
        }

    }

    public abstract void handleData(AppDataEvent appDataEvent);


}
