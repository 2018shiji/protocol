package com.module.protocol.application;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ApplicationManager implements IApplicationManager {
    private static List<IApplication> applicationList = new ArrayList<>();

    @Override
    public IApplication getApplicationByPort(int port) {
        for(int i = 0; i < applicationList.size(); i++){
            IApplication app = applicationList.get(i);
            if(app.getPort() == port)
                return app;
        }
        return null;
    }

    public static void addApplication(IApplication application){
        applicationList.add(application);
    }

}
