package com.pupa.dispatch.utils;

import com.pupa.dispatch.mapper.JobInfo;
import com.pupa.dispatch.service.JobService;
import com.pupa.jobs.CmdJob;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;

public class JobUtils {

    public static JobDetail initJob(JobInfo jobInfo) {
        int id = jobInfo.getId();
        int jobType = jobInfo.getType();//0:CMD JobInfo;1:Java JobInfo
        String name = jobInfo.getName();
        String group = jobInfo.getGroup();
        JobKey jobKey = JobKey.jobKey(name, group);

        JobDetail job = null;
        switch (jobType) {
            case 0://命令行Job
                job = JobBuilder.newJob(CmdJob.class).withIdentity(jobKey).build();
                break;
            case 1://Java Job
                String content = jobInfo.getContent();
                Class jobClass = Object.class;
                try {
                    jobClass = Class.forName(content);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                boolean isAssign = Job.class.isAssignableFrom(jobClass);//判断是否继承自Job
                if (isAssign) {
                    job = JobBuilder.newJob(jobClass).withIdentity(jobKey).build();
                }
                break;
            default:
                job = JobBuilder.newJob(CmdJob.class).withIdentity(jobKey).build();
                break;
        }

        if (null != job) {
            job.getJobDataMap().put(JobService.ID, id);
        }
        return job;
    }
}
