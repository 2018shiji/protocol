package com.module.protocol.application.tool;

import java.util.concurrent.BlockingQueue;

public class ScanInfoInput implements Runnable {
    private final BlockingQueue<ScanInfo> scanInfos;

    public ScanInfoInput(BlockingQueue<ScanInfo> scanInfos){
        this.scanInfos = scanInfos;
    }

    @Override
    public void run() {
        try{
            for(int i = 0; i < 65535; i++){
                scanInfos.put(new ScanInfo("www.baidu.com", i, false));
            }

            for(int i = 0; i < 65535; i++){
                scanInfos.put(new ScanInfo("127.0.0.1", i, true));
            }
        } catch (InterruptedException e){e.printStackTrace();}
    }
}
