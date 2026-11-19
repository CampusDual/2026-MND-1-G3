package com.campusdual.cd2026mnd1g3.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
    basePackages = "com.campusdual.cd2026mnd1g3.model.repository",
    excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.REGEX,
        pattern = ".*Patient(Owner|Directory).*Repository"
    ),
    entityManagerFactoryRef = "ganaderiaEntityManagerFactory",
    transactionManagerRef = "ganaderiaTransactionManager"
)
public class GanaderiaDataSourceConfig {
    @Primary
    @Bean(name = "ganaderiaDataSource")
    public DataSource ganaderiaDataSource(
            @Value("${spring.datasource.ganaderia.url:${spring.datasource.url}}") String jdbcUrl,
            @Value("${spring.datasource.ganaderia.username:${spring.datasource.username}}") String username,
            @Value("${spring.datasource.ganaderia.password:${spring.datasource.password:}}") String password,
            @Value("${spring.datasource.ganaderia.driver-class-name:${spring.datasource.driver-class-name}}") String driverClassName) {
        return DataSourceBuilder.create()
            .url(jdbcUrl)
            .username(username)
            .password(password)
            .driverClassName(driverClassName)
            .build();
    }

    @Bean(name = "ganaderiaFlyway")
    public Flyway ganaderiaFlyway(@Qualifier("ganaderiaDataSource") DataSource dataSource) {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration/ganaderia")
            .baselineOnMigrate(true)
            .load();
        flyway.migrate();
        return flyway;
    }

    @Primary
    @Bean(name = "ganaderiaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean ganaderiaEntityManagerFactory(
            @Qualifier("ganaderiaDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.campusdual.cd2026mnd1g3.model.entity");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        factory.setJpaPropertyMap(properties);
        return factory;
    }

    @Primary
    @Bean(name = "ganaderiaTransactionManager")
    public PlatformTransactionManager ganaderiaTransactionManager(
            @Qualifier("ganaderiaEntityManagerFactory") LocalContainerEntityManagerFactoryBean factory) {
        return new JpaTransactionManager(factory.getObject());
    }
}
