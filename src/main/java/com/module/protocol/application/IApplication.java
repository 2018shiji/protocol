package com.module.protocol.application;

import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public interface IApplication {
    int getPort();
    boolean isClosed();
    void handleData(HashMap<String, Object> data);
}
