package com.gym.crm.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfig {
    @Value("${liquibase.enabled:true}")
    private boolean liquibaseEnabled;

    @Value("${liquibase.changelog.master:db/migration/master.xml}")
    private String changelogPath;

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setShouldRun(liquibaseEnabled);

        if (liquibaseEnabled) {
            liquibase.setChangeLog(changelogPath);
        }

        return liquibase;
    }
}
