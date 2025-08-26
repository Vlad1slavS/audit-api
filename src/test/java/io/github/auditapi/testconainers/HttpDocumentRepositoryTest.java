package io.github.auditapi.testconainers;

import io.github.auditapi.model.elastic.HttpDocument;
import io.github.auditapi.repository.HttpDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.context.annotation.Import;
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
@Import(TestcontainersConfiguration.class)
class HttpDocumentRepositoryTest {

    @Container
    static ElasticsearchContainer elasticsearch = new ElasticsearchContainer("elasticsearch:9.1.2")
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
    }

    @Autowired
    private HttpDocumentRepository httpDocumentRepository;

    @BeforeEach
    void setUp() {
        httpDocumentRepository.deleteAll();

        List<HttpDocument> testData = List.of(
                HttpDocument.builder()
                        .id("1")
                        .uri("/api/orders")
                        .method("GET")
                        .statusCode("200")
                        .direction("INCOMING")
                        .requestBody("")
                        .responseBody("{\"orders\": []}")
                        .timestamp(LocalDateTime.now())
                        .build(),
                HttpDocument.builder()
                        .id("2")
                        .uri("/api/users/123")
                        .method("PUT")
                        .statusCode("404")
                        .direction("INCOMING")
                        .requestBody("{\"name\": \"John\"}")
                        .responseBody("{\"error\": \"User not found\"}")
                        .timestamp(LocalDateTime.now())
                        .build(),
                HttpDocument.builder()
                        .id("3")
                        .uri("/external/api/payment")
                        .method("POST")
                        .statusCode("200")
                        .direction("OUTGOING")
                        .requestBody("{\"amount\": 100}")
                        .responseBody("{\"status\": \"success\"}")
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        httpDocumentRepository.saveAll(testData);

        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> httpDocumentRepository.count() == 3);
    }

    @Test
    void findByStatusCode_ReturnCorrectResults() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<HttpDocument> results = httpDocumentRepository.findByStatusCode("200", pageable);

        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).extracting("uri")
                .contains("/api/orders", "/external/api/payment");
    }

    @Test
    void findByMethod_ReturnCorrectResults() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<HttpDocument> results = httpDocumentRepository.findByMethod("GET", pageable);

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().getFirst().getUri()).isEqualTo("/api/orders");
    }

    @Test
    void findByUriContaining_ReturnCorrectResults() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<HttpDocument> results = httpDocumentRepository.findByUriContaining("api", pageable);

        assertThat(results.getContent()).hasSize(3);
    }

    @Test
    void findByUriWildcard_ReturnCorrectResults() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<HttpDocument> results = httpDocumentRepository.findByUriWildcard("/api/*", pageable);

        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).extracting("uri")
                .contains("/api/orders", "/api/users/123");
    }

    @Test
    void findByMethodAndStatusCode_ReturnCorrectResults() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<HttpDocument> results = httpDocumentRepository.findByMethodAndStatusCode("POST", "200", pageable);

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().getFirst().getUri()).isEqualTo("/external/api/payment");
        assertThat(results.getContent().getFirst().getDirection()).isEqualTo("OUTGOING");
    }

}
