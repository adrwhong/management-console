/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * This configuration class ensures that the mc-config.properties are loaded before any downstream component
 * and/or configs read properties from the environment.
 *
 * @author Daniel Bernstein
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@PropertySource("${mc.config.file}") //this references the system property.
public class PropertyConfig {

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
        PropertySourcesPlaceholderConfigurer p = new PropertySourcesPlaceholderConfigurer();
        return p;
    }

}
