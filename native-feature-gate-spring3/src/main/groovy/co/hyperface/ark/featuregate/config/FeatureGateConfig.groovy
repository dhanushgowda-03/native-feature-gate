package co.hyperface.ark.featuregate.config

import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@ComponentScan(basePackages = ["co.hyperface.ark.featuregate"])
@EnableScheduling
class FeatureGateConfig {

    /**
     * Registers co.hyperface.ark.featuregate as a known package so Spring Boot's
     * JPA auto-configuration picks up our entities and repositories automatically.
     * Consuming services do not need @EntityScan or @EnableJpaRepositories.
     */
    @Bean
    static BeanDefinitionRegistryPostProcessor featureGatePackageRegistrar() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
                AutoConfigurationPackages.register(registry, "co.hyperface.ark.featuregate")
            }

            @Override
            void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {}
        }
    }
}
