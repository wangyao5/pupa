package com.pupa.dispatch.vo;

import com.pupa.dispatch.mapper.JobInfo;

public class Job {
    private String name;//名
    private String group;//分组
    private String description;//描述信息
    private String cron;//触发时间规则
    private String owner;//责任人列表
    private int type;//类型，0:CMD;1:Java JobInfo
    private String content;//type为CMD:content为CMD命令,type为Java JobInfo:content为java class全名xxx.xxx.xxx.class
    private String params;//job依赖的参数信息，可选填

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public JobInfo toJobInfo() {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setGroup(this.group);
        jobInfo.setName(this.name);
        jobInfo.setCron(this.cron);
        jobInfo.setDescription(this.description);
        jobInfo.setContent(this.content);
        jobInfo.setOwner(this.owner);
        jobInfo.setType(this.type);
        jobInfo.setCron(this.cron);
        jobInfo.setParams(this.params);
        return jobInfo;
    }
}
