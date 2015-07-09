package com.ofg.infrastructure.discovery.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer


@Configuration
class EmptyPropertySourceConfiguration {
    
    @Bean
    static PropertySourcesPlaceholderConfigurer propertiesConfigurer() {
        return new PropertySourcesPlaceholderConfigurer()
    }
}
