package com.gym.crm.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.util.Properties;

public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());

        Properties properties = factory.getObject();
        if (properties == null) {
            throw new IllegalStateException("Failed to load YAML properties from " + resource);
        }

        String sourceName = resource.getResource().getFilename();
        if (sourceName == null) {
            sourceName = "yaml-property-source";
        }

        return new PropertiesPropertySource(sourceName, properties);
    }
}
