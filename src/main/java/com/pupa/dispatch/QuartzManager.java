package com.pupa.dispatch;

import com.pupa.dispatch.mapper.JobInfo;
import com.pupa.dispatch.service.JobService;
import com.pupa.dispatch.utils.JobUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.List;

public class QuartzManager {
    @Autowired
    private SchedulerFactoryBean clusterSchedulerFactory;
    @Autowired
    private JobService jobService;

    public void init() throws SchedulerException {
        Scheduler scheduler = clusterSchedulerFactory.getScheduler();
        //查询所有可用job
        List <JobInfo> jobInfoList = jobService.findAll();

        for (JobInfo jobInfo : jobInfoList) {
            String name = jobInfo.getName();
            String group = jobInfo.getGroup();
            String cron = jobInfo.getCron();
            JobKey jobKey = JobKey.jobKey(name, group);

            JobDetail job = null;

            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }

            if (jobInfo.isEnable()) {
                if (!scheduler.checkExists(jobKey)) {
                    job = JobUtils.initJob(jobInfo);
                }

                if (null != job) {
                    Trigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group)
                            .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                            .startNow().build();
                    scheduler.scheduleJob(job, trigger);
                    scheduler.start();
                }
            }
        }
    }
}
