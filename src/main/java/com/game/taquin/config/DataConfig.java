package com.game.taquin.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configuration de la couche de données (Persistence).
 * Configure la connexion MySQL, Hibernate et la gestion des transactions.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.game.taquin.repository")
public class DataConfig {

    /**
     * Définit les paramètres de connexion à la base de données MySQL.
     */
    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        // Connexion locale sur l'adresse 127.0.0.1 (évite les bugs localhost sur Windows)
        dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/taquin_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        dataSource.setUsername("root");
        dataSource.setPassword(""); // Par défaut vide sur XAMPP/WAMP
        return dataSource;
    }

    /**
     * Configure l'EntityManager de JPA via Hibernate.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true); // Autorise la création auto des tables

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.game.taquin.model"); // Scanner les entités
        factory.setDataSource(dataSource());
        factory.setJpaProperties(hibernateProperties());
        return factory;
    }

    /**
     * Gestionnaire de transactions pour Spring.
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }

    /**
     * Propriétés spécifiques à Hibernate.
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "update"); // Met à jour la structure auto
        properties.setProperty("hibernate.show_sql", "true"); // Affiche les requêtes SQL dans la console
        properties.setProperty("hibernate.format_sql", "true"); // Formate le SQL pour lisibilité
        properties.setProperty("hibernate.connection.characterEncoding", "utf8");
        properties.setProperty("hibernate.connection.useUnicode", "true");
        return properties;
    }
}
