package com.gym.crm.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.sql.DataSource;

@DataJpaTest
@ActiveProfiles("test")
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
@Import(BaseTestRepository.DbUnitConfig.class)
abstract class BaseTestRepository<T> {
    @Autowired
    protected T repository;

    @Configuration
    static class DbUnitConfig {
        @Bean
        public DatabaseConfigBean dbUnitDatabaseConfig() {
            DatabaseConfigBean config = new DatabaseConfigBean();
            config.setQualifiedTableNames(false);

            return config;
        }

        @Bean
        public DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection(DataSource dataSource, DatabaseConfigBean dbUnitDatabaseConfig) {
            DatabaseDataSourceConnectionFactoryBean factory = new DatabaseDataSourceConnectionFactoryBean(dataSource);
            factory.setDatabaseConfig(dbUnitDatabaseConfig);
            factory.setSchema("PUBLIC");

            return factory;
        }
    }
}