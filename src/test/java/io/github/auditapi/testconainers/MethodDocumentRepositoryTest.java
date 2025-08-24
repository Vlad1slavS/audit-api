package io.github.auditapi.testconainers;

import io.github.auditapi.model.elastic.MethodDocument;
import io.github.auditapi.repository.MethodDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DataElasticsearchTest
@Testcontainers
class MethodDocumentRepositoryTest {

    @Container
    static ElasticsearchContainer elasticsearch = new ElasticsearchContainer("elasticsearch:9.1.2")
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
    }

    @Autowired
    private MethodDocumentRepository methodDocumentRepository;

    @BeforeEach
    void setUp() {
        methodDocumentRepository.deleteAll();

        List<MethodDocument> testData = List.of(
                MethodDocument.builder()
                        .method("getUserById")
                        .level("INFO")
                        .eventType("START")
                        .args("[123]")
                        .result("User{id=123}")
                        .timestamp(LocalDateTime.now())
                        .build(),
                MethodDocument.builder()
                        .method("Service.createUser")
                        .level("ERROR")
                        .eventType("START")
                        .args("[{name: 'John'}]")
                        .result(null)
                        .timestamp(LocalDateTime.now())
                        .build(),
                MethodDocument.builder()
                        .method("UserService.updateUser")
                        .level("DEBUG")
                        .eventType("START")
                        .args("[456, {name: 'Jane'}]")
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        methodDocumentRepository.saveAll(testData);

        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> methodDocumentRepository.count() == 3);
    }

    @Test
    void findByLevel_ReturnCorrectResults() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<MethodDocument> results = methodDocumentRepository.findByLevel("INFO", pageable);

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().getFirst().getMethod()).isEqualTo("getUserById");
    }

    @Test
    void findByMethodContaining_ReturnCorrectResults() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<MethodDocument> results = methodDocumentRepository.findByMethodContaining("Service", pageable);

        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).extracting("method")
                .contains("Service.createUser", "UserService.updateUser");
    }

    @Test
    void findByMethodWildcard_ReturnCorrectResults() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<MethodDocument> results = methodDocumentRepository.findByMethodWildcard("*Service.*", pageable);

        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).extracting("method")
                .contains("Service.createUser", "UserService.updateUser");
    }

    @Test
    void findByMethodContainingAndLevel_ReturnCorrectResults() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<MethodDocument> results = methodDocumentRepository.findByMethodContainingAndLevel("Service", "ERROR", pageable);

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().getFirst().getMethod()).isEqualTo("Service.createUser");
        assertThat(results.getContent().getFirst().getLevel()).isEqualTo("ERROR");
    }
}
