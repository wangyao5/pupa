package com.pupa.jobs;

import com.alibaba.fastjson.JSONArray;
import com.pupa.dispatch.mapper.JobInfo;
import com.pupa.dispatch.service.JobService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CmdJob implements Job {
    private Logger logger = LoggerFactory.getLogger(JobService.class);
    @Autowired
    private JobService jobService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        int id = jobDataMap.getIntValue(JobService.ID);
        //通过Id获取脚本命令执行
        JobInfo jobInfo = jobService.findOneJob(id);
        JSONArray cmdjsonArray = JSONArray.parseArray(jobInfo.getContent());//获取命令
        Thread thread = new Thread(() -> {
            int cmdSize = cmdjsonArray.size();
            for (int index = 0; index < cmdSize; index++) {
                String cmd = cmdjsonArray.getString(index);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss");
                String time = formatter.format(LocalDateTime.now());
                logger.info("-----------" + time);
                boolean interrupt = false;
                String s = null;
                try {
                    Process process = Runtime.getRuntime().exec(cmd);
                    BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    while ((s = stdOut.readLine()) != null || (s = errOut.readLine()) != null) {
                        logger.info(s);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.info(s);
                    interrupt = true;
                }

                if (interrupt) break;
            }
        });
        thread.start();
    }
}