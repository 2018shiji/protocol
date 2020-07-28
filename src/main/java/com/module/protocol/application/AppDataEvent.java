package com.module.protocol.application;

import org.springframework.context.ApplicationEvent;

import java.util.Map;

public class AppDataEvent extends ApplicationEvent {
    private Map<String, Object> headerInfo;

    public AppDataEvent(Object source) {
        super(source);
    }

    public AppDataEvent withHandleData(Map<String, Object> headerInfo){
        this.headerInfo = headerInfo;
        return this;
    }

    public Map<String, Object> getHeaderInfo(){
        return headerInfo;
    }
}
