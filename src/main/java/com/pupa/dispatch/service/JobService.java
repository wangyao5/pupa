package com.pupa.dispatch.service;

import com.pupa.dispatch.mapper.JobInfo;
import com.pupa.dispatch.mapper.JobMapper;
import com.pupa.dispatch.utils.JobUtils;
import com.pupa.dispatch.vo.Job;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional("transactionManager")
public class JobService {
    public static final String ID = "JOB_ID";
    private Logger logger = LoggerFactory.getLogger(JobService.class);
    @Autowired
    private JobMapper jobMapper;
    @Autowired
    private SchedulerFactoryBean clusterSchedulerFactory;

    public JobInfo findOneJob(int id) {
        return jobMapper.findOne(id);
    }

    public List <JobInfo> findAll() {
        return jobMapper.findAll();
    }

    public boolean addJob(Job job) {
        JobInfo jobInfo = job.toJobInfo();
        return jobMapper.insert(jobInfo);
    }

    /**
     * 更新Job ID的启用、暂停状态
     *
     * @param jobId  job ID
     * @param status 0:删除调度任务;1:启动调度任务
     */
    public boolean changeStatus(int jobId, int status) throws SchedulerException {
        Scheduler scheduler = clusterSchedulerFactory.getScheduler();
        //错误的开关状态值
        if (status != 0 && status != 1) {
            return false;
        }
        //判断jobId是否存在
        JobInfo jobInfo = jobMapper.findOne(jobId);
        if (null == jobInfo) {
            logger.info("jobId:" + jobId + ",不存在");
            return false;
        }

        JobKey jobKey = JobKey.jobKey(jobInfo.getName(), jobInfo.getGroup());
        if (status == 0) {
            jobInfo.setEnable(false);
            jobMapper.update(jobInfo);
            scheduler.deleteJob(jobKey);
        } else if (status == 1) {
            jobInfo.setEnable(true);
            jobMapper.update(jobInfo);
            //若该Job未启动,则启动该Job
            if (!scheduler.checkExists(jobKey)) {
                JobDetail job = JobUtils.initJob(jobInfo);
                if (null != job) {
                    Trigger trigger = TriggerBuilder.newTrigger().withIdentity(jobInfo.getName(), jobInfo.getGroup())
                            .withSchedule(CronScheduleBuilder.cronSchedule(jobInfo.getCron()))
                            .startNow().build();
                    scheduler.scheduleJob(job, trigger);
                    scheduler.start();
                } else {
                    logger.info("jobId:" + jobId + ",不可初始化");
                    return false;
                }
            } else {
                logger.info("jobId:" + jobId + ",重复启动");
                return false;
            }
        }
        return true;
    }

    /**
     * 删除无用Job
     *
     * @param jobId Job ID
     */
    public boolean delWithJobId(int jobId) throws SchedulerException {
        Scheduler scheduler = clusterSchedulerFactory.getScheduler();
        //判断jobId是否存在
        JobInfo jobInfo = jobMapper.findOne(jobId);
        if (null == jobInfo) {
            logger.info("jobId:" + jobId + ",不存在");
            return false;
        } else {
            jobMapper.del(jobId);
            JobKey jobKey = JobKey.jobKey(jobInfo.getName(), jobInfo.getGroup());
            if (scheduler.checkExists(jobKey)) {
                return scheduler.deleteJob(jobKey);
            }
        }

        return true;
    }

    public boolean update(int jobId, Job qJob) throws SchedulerException {
        Scheduler scheduler = clusterSchedulerFactory.getScheduler();
        JobInfo jobInfo = jobMapper.findOne(jobId);
        if (null == jobInfo) {
            logger.info("jobId:" + jobId + ",不存在");
            return false;
        }

        JobInfo updateJobInfo = qJob.toJobInfo();
        updateJobInfo.setId(jobId);
        //如果job名和job分组信息变化了，应该清除原任务重启新任务
        if (!jobInfo.getName().equals(updateJobInfo.getName()) || !jobInfo.getGroup().equals(updateJobInfo.getGroup())) {
            JobKey jobKey = JobKey.jobKey(jobInfo.getName(), jobInfo.getGroup());
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
                JobDetail job = JobUtils.initJob(jobInfo);
                if (null != job) {
                    Trigger trigger = TriggerBuilder.newTrigger().withIdentity(jobInfo.getName(), jobInfo.getGroup())
                            .withSchedule(CronScheduleBuilder.cronSchedule(jobInfo.getCron()))
                            .startNow().build();
                    scheduler.scheduleJob(job, trigger);
                    scheduler.start();
                } else {
                    logger.info("jobId:" + jobId + ",不可初始化");
                    return false;
                }
            }
        }
        return jobMapper.update(updateJobInfo);
    }

    public List <JobInfo> findJobs(String message, Integer type, String enable, int start, int size) {
        if (enable == null) {
            return jobMapper.findQuery(message, type, null, start, size);
        }
        return jobMapper.findQuery(message, type, Boolean.valueOf(enable), start, size);
    }

    public int countJob(String message, Integer type, String enable) {
        if (enable == null) {
            return jobMapper.count(message, type, null);
        }
        return jobMapper.count(message, type, Boolean.valueOf(enable));
    }
}
