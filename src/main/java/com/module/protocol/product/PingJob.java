package com.module.protocol.product;

import com.module.protocol.application.appImpl.PingApp;
import com.module.protocol.datalink.DataLinkLayer;
import jpcap.JpcapCaptor;
import lombok.Setter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import static com.module.protocol.product.DeviceMonitor.REMOTE_ADDR_KEY;


@Setter
@Component
public class PingJob implements Job, ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        //"192.168.50.200"
        System.out.println("here" + " 00000000 " + jobExecutionContext.getJobDetail().getJobDataMap().get(REMOTE_ADDR_KEY));
        applicationContext.getBean(PingApp.class).startPing("192.168.50.1",
                (String)jobExecutionContext.getJobDetail().getJobDataMap().get(REMOTE_ADDR_KEY));
        System.out.println("job final ****** ************* ********** *****");
    }

}
