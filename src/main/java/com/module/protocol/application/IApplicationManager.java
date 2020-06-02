package com.module.protocol.application;

import org.springframework.stereotype.Component;

@Component
public interface IApplicationManager {
    IApplication getApplicationByPort(int port);
}
