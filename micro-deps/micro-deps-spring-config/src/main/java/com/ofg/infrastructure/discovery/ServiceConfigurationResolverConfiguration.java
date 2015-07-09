package com.ofg.infrastructure.discovery;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ServiceConfigurationResolverConfiguration {

    @Bean
    ServiceConfigurationResolver serviceConfigurationResolver(
            @Value("${microservice.config.file:classpath:microservice.json}") Resource microserviceConfig
    ) throws IOException {
        return new ServiceConfigurationResolver(IOUtils.toString(microserviceConfig.getInputStream()));
    }

}
