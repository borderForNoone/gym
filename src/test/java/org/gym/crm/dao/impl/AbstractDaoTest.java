package org.gym.crm.dao.impl;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.gym.crm.config.TestAppConfig;
import org.gym.crm.config.DbUnitConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class})
@SpringJUnitConfig(classes = {TestAppConfig.class, DbUnitConfig.class})
@DbUnitConfiguration(databaseConnection = "dbUnitDatabaseConnection")
public abstract class AbstractDaoTest<T> {
    @Autowired
    protected ApplicationContext context;

    protected T dao;

    protected abstract Class<T> getDaoClass();

    @BeforeEach
    void initDbUnit() throws Exception {
        dao = context.getBean(getDaoClass());
    }
}