package com.pupa;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * dispatch mybatis 设置
 */
@Configuration
@MapperScan(basePackages = {"com.pupa.dispatch.mapper"}, sqlSessionFactoryRef = "dispatchSqlSession")
public class DispatchMapperConfig {

    @Value("${dispatch.sql.std.enable}")
    private boolean enableStd;

    @Value("${dispatch.mapper.locations}")
    private String location;

    @Bean(name = "dispatchSqlSession")
    public SqlSessionFactory dispatchSqlSession(@Qualifier("dispatchDataSource") DataSource dispatchDataSource) throws Exception {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        if (enableStd) {
            org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
            configuration.setLogImpl(StdOutImpl.class);
            sessionFactoryBean.setConfiguration(configuration);
        }
        sessionFactoryBean.setDataSource(dispatchDataSource);
        sessionFactoryBean.setTransactionFactory(new SpringManagedTransactionFactory());
        sessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(location));

        return sessionFactoryBean.getObject();
    }

    @Bean(name = "transactionManager")
    public DataSourceTransactionManager transactionManager(@Qualifier("dispatchDataSource") DataSource dispatchDataSource) {
        return new DataSourceTransactionManager(dispatchDataSource);
    }

    @Bean(name = "dispatchDataSource")
    @Qualifier
    public DruidDataSource dispatchDataSource(@Value("${dispatch.datasource.driverClassName}") String dirverClassName,
                                         @Value("${dispatch.datasource.url}") String dataSourceUrl,
                                         @Value("${dispatch.datasource.username}") String userName,
                                         @Value("${dispatch.datasource.password}") String password,
                                         @Value("${dispatch.druid.filters}") String filters) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dirverClassName);
        dataSource.setUrl(dataSourceUrl);
        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        if (null == filters || filters.trim().isEmpty()){
            filters = "stat";
        }
        try {
            dataSource.setFilters(filters);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataSource;
    }
}
