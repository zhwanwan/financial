package com.imooc.seller.com.imooc.seller.configuration;

import com.imooc.entity.Order;
import com.imooc.seller.repositories.OrderRepository;
import com.imooc.seller.repositoriesbackup.VerifyRepository;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.SchemaManagement;
import org.springframework.boot.jdbc.SchemaManagementProvider;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源配置--多数据源
 */
@Configuration
public class DataAccessConfiguration {

    @Autowired
    private JpaProperties properties;
    @Autowired
    private ObjectProvider<List<SchemaManagementProvider>> providers;
    @Autowired
    private ObjectProvider<PhysicalNamingStrategy> physicalNamingStrategy;
    @Autowired
    private ObjectProvider<ImplicitNamingStrategy> implicitNamingStrategy;

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.primary")
    public DataSourceProperties primayDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.backup")
    public DataSourceProperties backupDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource primaryDataSource() {
        return primayDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public DataSource backupDataSource() {
        return backupDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("primaryDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource)
                .packages(Order.class)
                .properties(getVendorProperties(dataSource))
                .persistenceUnit("primary")
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean backupEntityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("backupDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource)
                .packages(Order.class)
                .properties(getVendorProperties(dataSource))
                .persistenceUnit("backup")
                .build();
    }

    private Map<String, Object> getVendorProperties(DataSource dataSource) {
        Map<String, Object> vendorProperties = new LinkedHashMap<String, Object>();
        String defaultDdlMode = new HibernateDefaultDdlAutoProvider(
                providers.getIfAvailable(Collections::emptyList))
                .getDefaultDdlAuto(dataSource);
        vendorProperties.putAll(properties.getHibernateProperties(
                new HibernateSettings().ddlAuto(defaultDdlMode).physicalNamingStrategy(physicalNamingStrategy.getIfAvailable())
                        .implicitNamingStrategy(implicitNamingStrategy.getIfAvailable())
        ));
        return vendorProperties;
    }

    @Bean
    public PlatformTransactionManager primaryTransactionManager(@Qualifier("primaryEntityManagerFactory") LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory) {
        return new JpaTransactionManager(primaryEntityManagerFactory.getObject());
    }

    @Bean
    public PlatformTransactionManager backupTransactionManager(@Qualifier("backupEntityManagerFactory") LocalContainerEntityManagerFactoryBean backupEntityManagerFactory) {
        return new JpaTransactionManager(backupEntityManagerFactory.getObject());
    }

    @EnableJpaRepositories(
            basePackageClasses = OrderRepository.class,
            entityManagerFactoryRef = "primaryEntityManagerFactory",
            transactionManagerRef = "primaryTransactionManager"
    )
    public class PrimaryConfiguration {
    }

    @EnableJpaRepositories(
            basePackageClasses = VerifyRepository.class,
            entityManagerFactoryRef = "backupEntityManagerFactory",
            transactionManagerRef = "backupTransactionManager"
    )
    public class BackupConfiguration {
    }


    class HibernateDefaultDdlAutoProvider implements SchemaManagementProvider {

        private final List<SchemaManagementProvider> providers;

        HibernateDefaultDdlAutoProvider(List<SchemaManagementProvider> providers) {
            this.providers = providers;
        }

        public String getDefaultDdlAuto(DataSource dataSource) {
            if (!EmbeddedDatabaseConnection.isEmbedded(dataSource)) {
                return "none";
            }
            SchemaManagement schemaManagement = getSchemaManagement(dataSource);
            if (SchemaManagement.MANAGED.equals(schemaManagement)) {
                return "none";
            }
            return "create-drop";

        }

        @Override
        public SchemaManagement getSchemaManagement(DataSource dataSource) {
            for (SchemaManagementProvider provider : this.providers) {
                SchemaManagement schemaManagement = provider.getSchemaManagement(dataSource);
                if (SchemaManagement.MANAGED.equals(schemaManagement)) {
                    return schemaManagement;
                }
            }
            return SchemaManagement.UNMANAGED;
        }

    }


}
