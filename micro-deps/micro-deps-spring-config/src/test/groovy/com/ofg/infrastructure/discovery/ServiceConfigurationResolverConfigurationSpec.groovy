package com.ofg.infrastructure.discovery
import com.ofg.infrastructure.discovery.config.EmptyPropertySourceConfiguration
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource
import org.springframework.core.io.Resource
import spock.lang.Specification

class ServiceConfigurationResolverConfigurationSpec extends Specification {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()
    private static String microserviceJsonProperty = 'microservice.config.file'

    def 'should be able to fetch microservice.json from location provided in yaml'() {
        given:
        context.register(
                DependencyResolutionYamlConfiguration,
                ServiceConfigurationResolverConfiguration
        )
        when:
        context.refresh()
        then:
        assertThatNonDefaultAndExistingMicroserviceJsonIsAvailableIn(context)
        cleanup:
        context?.close()
    }

    def 'should be able to fetch microservice.json from location provided in System property'() {
        given:
        System.setProperty(microserviceJsonProperty, 'classpath:service-configuration-resolver-microservice.json')
        context.register(
                EmptyPropertySourceConfiguration,
                ServiceConfigurationResolverConfiguration
        )
        when:
        context.refresh()
        then:
        assertThatNonDefaultAndExistingMicroserviceJsonIsAvailableIn(context);
        cleanup:
        System.clearProperty(microserviceJsonProperty)
        context?.close()
    }

    @Configuration
    @PropertySource('classpath:service-configuration-resolver-config.yaml')
    @Import(EmptyPropertySourceConfiguration)
    public static class DependencyResolutionYamlConfiguration {

    }

    void assertThatNonDefaultAndExistingMicroserviceJsonIsAvailableIn(AnnotationConfigApplicationContext context) {
        Resource resource = getMicroserviceConfigResource(context)
        assertThatExist(resource)
        assetThatNonDefaultWasFetched(resource)
    }

    private Resource getMicroserviceConfigResource(AnnotationConfigApplicationContext context) {
        String microserviceJsonLocation = context.environment.getProperty(microserviceJsonProperty)
        return context.getResource(microserviceJsonLocation)
    }

    private void assertThatExist(Resource cfg) {
        assert cfg.exists() == true
    }

    private void assetThatNonDefaultWasFetched(Resource cfg) {
        assert cfg.filename.endsWith('-microservice.json') == true
    }

}
