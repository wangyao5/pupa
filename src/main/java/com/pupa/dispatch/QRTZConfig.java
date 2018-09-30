package com.pupa.dispatch;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.concurrent.RejectedExecutionHandler;

@Configuration
public class QRTZConfig {
    @Bean(name = "threadPoolExecutor")
    public ThreadPoolTaskExecutor threadPoolExecutor(@Value("${qrtz.thread.pool.corePoolSize}") int corePoolSize,
                                                     @Value("${qrtz.thread.pool.maxPoolSize}") int maxPoolSize,
                                                     @Value("${qrtz.thread.pool.queueCapacity}") int queueCapacity,
                                                     @Value("${qrtz.thread.pool.keepAliveSeconds}") int keepAliveSeconds,
                                                     @Value("${qrtz.thread.pool.rejectedExecutionHandler}") String rejectedExecutionHandler) {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);//核心线程数
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);//最大线程数
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);//队列最大长度
        threadPoolTaskExecutor.setKeepAliveSeconds(keepAliveSeconds);//线程池维护线程所允许的空闲时间
        RejectedExecutionHandler handler = null;
        try {
            handler = (RejectedExecutionHandler) Class.forName(rejectedExecutionHandler).newInstance();//拒绝任务策略:被拒绝后直接在调用者线程中运行当前被放弃任务
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        threadPoolTaskExecutor.setRejectedExecutionHandler(handler);
        return threadPoolTaskExecutor;
    }

    @Bean("jobFactory")
    public JobFactory jobFactory() {
        return new JobFactory();
    }

    @Bean("clusterSchedulerFactory")
    public SchedulerFactoryBean clusterSchedulerFactory(@Qualifier("dispatchDataSource") DataSource dispatchDataSource,
                                                        @Qualifier("threadPoolExecutor") ThreadPoolTaskExecutor threadPoolExecutor,
                                                        @Qualifier("transactionManager") DataSourceTransactionManager transactionManager,
                                                        @Qualifier("jobFactory") JobFactory jobFactory) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setDataSource(dispatchDataSource);
        schedulerFactoryBean.setTaskExecutor(threadPoolExecutor);
        schedulerFactoryBean.setTransactionManager(transactionManager);
        schedulerFactoryBean.setConfigLocation(new PathMatchingResourcePatternResolver().getResource("quartz.properties"));
        schedulerFactoryBean.setSchedulerName("CacheCloudScheduler");
        schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContext");
        schedulerFactoryBean.setOverwriteExistingJobs(true);
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(false);
        schedulerFactoryBean.setStartupDelay(0);
        schedulerFactoryBean.setAutoStartup(true);
        schedulerFactoryBean.setJobDetails();
        schedulerFactoryBean.setJobFactory(jobFactory);
        return schedulerFactoryBean;
    }

    @Bean(name = "quartzManager", initMethod = "init")
    @Lazy(false)
    public QuartzManager quartzManager() {
        return new QuartzManager();
    }

}
