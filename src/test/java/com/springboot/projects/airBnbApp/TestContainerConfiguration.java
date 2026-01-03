package com.springboot.projects.airBnbApp;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@TestConfiguration
public class TestContainerConfiguration {

    @Bean
    @ServiceConnection
        // Auto-configures datasource properties
    PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\s", 2))
                .withStartupTimeout(Duration.ofSeconds(120));
//                .withDatabaseName("testdb")
//                .withUsername("testuser")
//                .withPassword("testpass")
//                .withReuse(true);  // Keep container alive between runs
    }
}
