package org.gym.crm.config;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import org.gym.crm.filter.TransactionLoggingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{AppConfig.class, DataSourceConfig.class, HibernateConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{WebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected void registerContextLoaderListener(ServletContext servletContext) {
        super.registerContextLoaderListener(servletContext);

        FilterRegistration.Dynamic loggingFilter = servletContext.addFilter("transactionLoggingFilter", new TransactionLoggingFilter());
        loggingFilter.addMappingForUrlPatterns(null, false, "/*");
    }
}
