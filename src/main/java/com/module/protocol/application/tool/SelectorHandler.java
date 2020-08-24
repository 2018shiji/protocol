package com.module.protocol.application.tool;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectorHandler extends Thread {
    private Selector selector;
    private AtomicInteger count;

    public SelectorHandler(Selector selector, AtomicInteger count){
        this.selector = selector;
        this.count = count;
    }

    @Override
    public void run() {
        while(true){
            try{
                if(count.get() == 50){
                    System.out.println("finish port scan");
                    break;
                }
                handleScanInfo();
            } catch (Exception e){e.printStackTrace();}
        }
    }

    private void handleScanInfo(){
        SocketChannel channel = null;
        try{
            int readyChannels = selector.select(1000);
            System.out.println(readyChannels);
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while(it.hasNext()){
                SelectionKey key = it.next();
                it.remove();
                if(channel.isConnectionPending()){
                    try{
                        if(channel.finishConnect())
                            System.out.println(channel.getRemoteAddress().toString() + " ok");
                        else{
                            key.cancel();
                            System.out.println(channel.getRemoteAddress().toString() + " failed");
                        }
                    } catch (Exception e) {
                        System.out.println(channel.toString() + " failed" + Thread.currentThread().getName());
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if(channel != null){
                try{
                    channel.close();
                }catch (IOException e){e.printStackTrace();}
            }
        }
    }
}
