package com.module.protocol.application;

import com.module.protocol.IApplication;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class Application implements IApplication {

    protected int port = 0;
    private boolean closed = false;

    public Application(){
        ApplicationManager.getInstance().addApplication(this);
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
