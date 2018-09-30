package com.pupa.dispatch.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobMapper {

    List <JobInfo> findAll();

    boolean insert(@Param("jobInfo") JobInfo jobInfo);

    boolean update(@Param("jobInfo") JobInfo jobInfo);

    boolean del(@Param("id") int id);

    JobInfo findOne(@Param("id") int id);

    List <JobInfo> findQuery(@Param("message") String message, @Param("type") Integer type, @Param("enable") Boolean enable, @Param("start") int start, @Param("size") int size);

    int count(@Param("message") String message, @Param("type") Integer type, @Param("enable") Boolean enable);
}
