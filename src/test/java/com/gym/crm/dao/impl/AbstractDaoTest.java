package com.gym.crm.dao.impl;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.gym.crm.config.DbUnitConfig;
import com.gym.crm.config.TestAppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class})
@SpringJUnitConfig(classes = {TestAppConfig.class, DbUnitConfig.class})
@DbUnitConfiguration(databaseConnection = "dbUnitDatabaseConnection")
public abstract class AbstractDaoTest<T> {
    protected T dao;

    @Autowired
    public void setDao(T dao) {
        this.dao = dao;
    }
}