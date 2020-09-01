package com.module.protocol.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PingConsumer extends Thread {
    @Autowired PingMQueue pingMQueue;

    @Override
    public void run() {
        try {
            while(true) {
                System.out.println("PingConsumer consume -----------------> " +
                        pingMQueue.getQueue().take().getRemoteIP());
            }
        } catch (Exception e){ e.printStackTrace();}
    }
}
