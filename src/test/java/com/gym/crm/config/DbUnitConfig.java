package com.gym.crm.config;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DbUnitConfig {
    @Bean
    public DatabaseConfigBean dbUnitDatabaseConfig() {
        DatabaseConfigBean config = new DatabaseConfigBean();
        config.setCaseSensitiveTableNames(false);
        config.setQualifiedTableNames(false);

        return config;
    }

    @Bean
    public DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection(DataSource dataSource, DatabaseConfigBean dbUnitDatabaseConfig) {
        var bean = new DatabaseDataSourceConnectionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setSchema("PUBLIC");
        bean.setDatabaseConfig(dbUnitDatabaseConfig);

        return bean;
    }
}
