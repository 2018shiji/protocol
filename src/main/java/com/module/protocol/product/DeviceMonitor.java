package com.module.protocol.product;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeviceMonitor {
    public static final String REMOTE_ADDR_KEY = "finalRemoteAddr";

    public static void doPingJob(List<String> remoteAddr){
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();

            scheduler.scheduleJob(withPingJobDetail("10.28.56.121"),
                    withTriggerPerSeconds(5));

            scheduler.scheduleJob(withPingJobDetail("10.28.56.122"),
                    withTriggerPerSeconds(5));

            scheduler.scheduleJob(withPingJobDetail("10.28.56.123"),
                    withTriggerPerSeconds(5));

            scheduler.scheduleJob(withPingJobDetail("10.28.56.124"),
                    withTriggerPerSeconds(5));

            scheduler.scheduleJob(withPingJobDetail("10.28.56.125"),
                    withTriggerPerSeconds(5));

            scheduler.scheduleJob(withPingJobDetail("10.28.56.126"),
                    withTriggerPerSeconds(5));

            scheduler.scheduleJob(withPingJobDetail("10.28.56.127"),
                    withTriggerPerSeconds(5));

            scheduler.scheduleJob(withPingJobDetail("10.28.56.128"),
                    withTriggerPerSeconds(5));

            scheduler.scheduleJob(withPingJobDetail("10.28.56.129"),
                    withTriggerPerSeconds(5));

            scheduler.scheduleJob(withPingJobDetail("10.28.56.130"),
                    withTriggerPerSeconds(5));


            JobDetail jobDetail2 = JobBuilder.newJob(TestJob.class).build();
            scheduler.scheduleJob(jobDetail2, withTriggerPerSeconds(5));
        }catch (Exception e) {e.printStackTrace();}

    }

    public static JobDetail withPingJobDetail(String remoteAddr){
        JobDetail jobDetail = JobBuilder.newJob(PingJob.class)
                .usingJobData(REMOTE_ADDR_KEY, remoteAddr)
                .build();
        return jobDetail;
    }

    public static Trigger withTriggerPerSeconds(int seconds){
        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule("0/" + seconds + " * * * * ? 2020"))
                .build();
        return trigger;
    }

    public static void testJob(){
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();
            JobDetail jobDetail = JobBuilder.newJob(TestJob.class).build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .startNow()
                    .withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ? 2020"))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
        }catch (Exception e) {e.printStackTrace();}
    }

}
