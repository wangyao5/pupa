package com.pupa.dispatch.controller;

import com.pupa.ApplicationContextProvider;
import com.pupa.DispatchException;
import com.pupa.dispatch.mapper.JobInfo;
import com.pupa.dispatch.service.JobService;
import com.pupa.dispatch.vo.RStatus;
import com.pupa.jobs.CmdJob;
import org.quartz.CronExpression;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;
import org.quartz.impl.JobDetailImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调度设置
 */
@RestController
@CrossOrigin
@RequestMapping("/dispatch")
public class QRTZContoller {
    @Autowired
    private JobService jobService;

    @Autowired
    private ApplicationContextProvider applicationContextProvider;

    /**
     * 新配置一个Job
     */
    @PostMapping("/add/job")
    public Map addJob(@RequestBody com.pupa.dispatch.vo.Job job) {
        RStatus rStatus = new RStatus(RStatus.SUCCESS);
        try {
            CronExpression cronExpression = new CronExpression(job.getCron());
        } catch (Exception e) {
            throw new DispatchException("cron参数格式不正确:" + e.getMessage(), e);
        }

        try {
            jobService.addJob(job);
        } catch (Exception e) {
            throw new DispatchException("新配置一个Job:" + e.getMessage(), e);
        }
        return rStatus.convertMap();
    }

    /**
     * 修改指定Job的状态
     *
     * @param jobId  jobId主键
     * @param status 状态值，0:无效，1:有效
     */
    @PostMapping("/change/job/{jobId}/{status}")
    public Map changeStatus(@PathVariable("jobId") int jobId, @PathVariable(value = "status") int status) {
        RStatus <String> rStatus = new RStatus <>(RStatus.SUCCESS);
        boolean rs = false;
        try {
            rs = jobService.changeStatus(jobId, status);
        } catch (Exception e) {
            throw new DispatchException("修改指定Job的状态:" + e.getMessage(), e);
        }
        if (!rs) {
            rStatus.setCode(RStatus.FATAL);
            rStatus.setMessage("修改指定Job的状态失败!");
        }

        return rStatus.convertMap();
    }

    /***
     * 更改job信息
     * @param jobId jobId
     * @param name 名称
     * @param group 分组名
     * @param content 内容
     * @param description 描述信息
     * @param owner 负责人
     * @param type 类型:0:cmd,1:java Job
     * @param cron 定时规则
     * @param params 参数信息
     */
    @PostMapping("/update/job/{jobId}")
    public Map updateJobInfo(@PathVariable("jobId") int jobId, @RequestParam("name") String name,
                             @RequestParam("group") String group, @RequestParam(value = "content", required = false) String content,
                             @RequestParam(value = "description", required = false) String description, @RequestParam(value = "owner", required = false) String owner,
                             @RequestParam(value = "type", required = false) String type, @RequestParam(value = "cron", required = false) String cron,
                             @RequestParam(value = "params", required = false) String params) {
        RStatus <String> rStatus = new RStatus <>(RStatus.SUCCESS);
        boolean rs = false;
        com.pupa.dispatch.vo.Job job = new com.pupa.dispatch.vo.Job();
        job.setName(name);
        job.setGroup(group);
        job.setContent(content);
        job.setDescription(description);
        job.setOwner(owner);
        job.setType(Integer.valueOf(type));
        job.setCron(cron);
        job.setParams(params);
        try {
            if (job.getCron() != null) {
                CronExpression cronExpression = new CronExpression(job.getCron());
            }
        } catch (Exception e) {
            throw new DispatchException("cron参数格式不正确:" + e.getMessage(), e);
        }

        try {
            rs = jobService.update(jobId, job);
        } catch (Exception e) {
            throw new DispatchException("更新job信息失败:" + e.getMessage(), e);
        }

        if (!rs) {
            rStatus.setCode(RStatus.FATAL);
            rStatus.setMessage("修改Job信息失败!");
        }
        return rStatus.convertMap();
    }

    /**
     * 删除无用Job
     *
     * @param jobId jobId主键
     */
    @PostMapping("/del/job/{jobId}")
    public Map del(@PathVariable("jobId") int jobId) {
        RStatus rStatus = new RStatus(RStatus.SUCCESS);
        try {
            jobService.delWithJobId(jobId);
        } catch (Exception e) {
            throw new DispatchException("删除无用Job:" + e.getMessage(), e);
        }
        return rStatus.convertMap();
    }

    /**
     * 立即启动Job
     *
     * @param jobId
     * @return
     */
    @PostMapping("/start/job/{jobId}")
    public Map startJob(@PathVariable("jobId") int jobId) {
        RStatus rStatus = new RStatus(RStatus.SUCCESS);
        JobInfo jobInfo = jobService.findOneJob(jobId);
        int jobType = jobInfo.getType();
        //构建参数
        JobDetailImpl jobDetail = new JobDetailImpl();
        Map data = new HashMap();
        data.put(JobService.ID, jobInfo.getId());
        JobDataMap jobDataMap = new JobDataMap(data);
        jobDetail.setJobDataMap(jobDataMap);
        final ProxyJobExecutionContextImp jobExecutionContentFinal = new ProxyJobExecutionContextImp();
        jobExecutionContentFinal.setJobDetail(jobDetail);
        try {
            switch (jobType) {
                case 0://命令行Job
                    Runnable cmdRunnable = new Runnable() {
                        @Override
                        public void run() {
                            org.quartz.Job cmd = (org.quartz.Job) applicationContextProvider.getBean(CmdJob.class);
                            try {
                                cmd.execute(jobExecutionContentFinal);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    Thread cmdThread = new Thread(cmdRunnable);
                    cmdThread.start();
                    break;
                case 1://Java Job
                    Runnable javaRunnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Class jobClass = Class.forName(jobInfo.getContent());
                                boolean isAssign = org.quartz.Job.class.isAssignableFrom(jobClass);//判断是否继承自Job
                                if (isAssign) {
                                    org.quartz.Job javaJob = (org.quartz.Job) applicationContextProvider.getBean(jobClass);
                                    javaJob.execute(jobExecutionContentFinal);
                                }
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (JobExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    Thread javaThread = new Thread(javaRunnable);
                    javaThread.start();
            }
        } catch (Exception e) {
            throw new DispatchException("启动失败:" + e.getMessage(), e);
        }
        return rStatus.convertMap();
    }

    /**
     * 条件查询Job
     *
     * @param page
     * @param pageSize
     * @param enable   是否有效
     * @param type     类型
     * @param message  名称，描述，责任人
     * @return
     */
    @GetMapping("/find/{page}/{pageSize}")
    public Map list(@PathVariable("page") int page, @PathVariable("pageSize") int pageSize,
                    @RequestParam(value = "enable", required = false) String enable,
                    @RequestParam(value = "type", required = false) Integer type,
                    @RequestParam(value = "message", required = false) String message) {
        RStatus rStatus = new RStatus(RStatus.SUCCESS);
        try {
            List <JobInfo> lists = jobService.findJobs(message, type, enable, page * pageSize, pageSize);
            rStatus.setBody(lists);
        } catch (Exception e) {
            throw new DispatchException("查询Job:" + e.getMessage(), e);
        }

        return rStatus.convertMap();

    }

    /**
     * 条件查询Job数目
     *
     * @param enable  是否有效
     * @param type    类型
     * @param message 名称，描述，责任人
     * @return
     */
    @GetMapping("/find/count")
    public Map countJob(@RequestParam(value = "enable", required = false) String enable,
                        @RequestParam(value = "type", required = false) Integer type,
                        @RequestParam(value = "message", required = false) String message) {
        RStatus rStatus = new RStatus(RStatus.SUCCESS);
        try {
            int count = jobService.countJob(message, type, enable);
            rStatus.setBody(count);
        } catch (Exception e) {
            throw new DispatchException("查询Job:" + e.getMessage(), e);
        }

        return rStatus.convertMap();

    }
}
