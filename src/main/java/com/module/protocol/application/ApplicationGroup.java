package com.module.protocol.application;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter
public class ApplicationGroup {
    private List<Application> ipAppList = new ArrayList<>();
    private List<Application> udpAppList = new ArrayList<>();
    private List<Application> icmpAppList = new ArrayList<>();

    public List<Application> registerToIPList(Application app){ ipAppList.add(app);return ipAppList; }

    public List<Application> registerToUDPAppList(Application app){ udpAppList.add(app);return udpAppList; }

    public List<Application> registerToICMPList(Application app){ icmpAppList.add(app);return icmpAppList; }
}
