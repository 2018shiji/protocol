package com.module.protocol.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class Application implements IApplication {

    @Autowired
    private ApplicationManager manager;
    protected int port = 0;
    private boolean closed = false;

    public Application(){
        manager.addApplication(this);
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void handleData(HashMap<String, Object> data) {

    }
}
