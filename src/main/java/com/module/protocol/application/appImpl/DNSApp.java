package com.module.protocol.application.appImpl;

import com.module.protocol.application.AppDataEvent;
import com.module.protocol.application.Application;

public class DNSApp extends Application {
    private byte[] resolve_server_ip = null;
    private String domainName = "";
    private byte[] dnsHeader = null;
    private short transition_id = 0;
    private byte[] dnsQuestion = null;

    @Override
    public void handleData(AppDataEvent appDataEvent) {

    }
}
