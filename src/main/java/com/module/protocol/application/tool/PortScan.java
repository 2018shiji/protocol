package com.module.protocol.application.tool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PortScan extends Thread {
    private AtomicInteger off;
    private Selector selector;
    private final BlockingQueue<ScanInfo> scanInfos;

    public PortScan(BlockingQueue<ScanInfo> scanInfos, Selector selector, AtomicInteger off){
        this.off = off;
        this.selector = selector;
        this.scanInfos = scanInfos;
    }

    @Override
    public void run() {
        while(true){
            try{
                ScanInfo scanInfo = scanInfos.take();
                if(scanInfo.endFlag){
                    System.out.println("Client end ！" + Thread.currentThread().getName());
                    off.getAndIncrement();
                    break;
                }
                registerOPConnect(scanInfo);
            } catch (Exception e){e.printStackTrace();}
        }
    }

    private void registerOPConnect(ScanInfo scanInfo){
        SocketChannel channel = null;
        try{
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(scanInfo.ip, scanInfo.port));
            channel.register(selector, SelectionKey.OP_CONNECT);
        } catch (IOException e){e.printStackTrace();}
    }
}
