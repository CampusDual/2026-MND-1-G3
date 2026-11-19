package com.campusdual.cd2026mnd1g3.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    basePackages = "com.campusdual.cd2026mnd1g3.model.clinica",
    entityManagerFactoryRef = "clinicaEntityManagerFactory",
    transactionManagerRef = "clinicaTransactionManager"
)
public class ClinicaDataSourceConfig {
    @Bean(name = "clinicaDataSource")
    public DataSource clinicaDataSource(
            @Value("${spring.datasource.clinica.url:${spring.datasource.url}}") String jdbcUrl,
            @Value("${spring.datasource.clinica.username:${spring.datasource.username}}") String username,
            @Value("${spring.datasource.clinica.password:${spring.datasource.password:}}") String password,
            @Value("${spring.datasource.clinica.driver-class-name:${spring.datasource.driver-class-name}}") String driverClassName) {
        return DataSourceBuilder.create()
            .url(jdbcUrl)
            .username(username)
            .password(password)
            .driverClassName(driverClassName)
            .build();
    }

    @Bean(name = "clinicaFlyway")
    public Flyway clinicaFlyway(@Qualifier("clinicaDataSource") DataSource dataSource) {
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration/clinica")
            .baselineOnMigrate(true)
            .load();
        flyway.migrate();
        return flyway;
    }

    @Bean(name = "clinicaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean clinicaEntityManagerFactory(
            @Qualifier("clinicaDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.campusdual.cd2026mnd1g3.model.clinica");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        factory.setJpaPropertyMap(properties);
        return factory;
    }

    @Bean(name = "clinicaTransactionManager")
    public PlatformTransactionManager clinicaTransactionManager(
            @Qualifier("clinicaEntityManagerFactory") LocalContainerEntityManagerFactoryBean factory) {
        return new JpaTransactionManager(factory.getObject());
    }
}
