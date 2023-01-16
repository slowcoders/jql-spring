package org.eipgrid.jql.sample.test;

import org.springframework.core.io.ClassPathResource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class TestEnv {

    public static void startPostgres() {
        try {
            ClassPathResource resource = new ClassPathResource("docker-compose.yaml");
            DockerComposeContainer container = new DockerComposeContainer(resource.getFile());
            container.waitingFor("postgres",
                    Wait.forLogMessage(".*database system is ready to accept connections.*", 1));
            container.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start postgres container", e);
        }
    }
}
