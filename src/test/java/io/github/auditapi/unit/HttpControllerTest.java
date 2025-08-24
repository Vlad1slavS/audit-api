package io.github.auditapi.unit;

import io.github.auditapi.controller.v1.HttpController;
import io.github.auditapi.model.elastic.HttpDocument;
import io.github.auditapi.model.elastic.SearchResponse;
import io.github.auditapi.model.elastic.StatsResponse;
import io.github.auditapi.service.HttpSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpControllerTest {

    @Mock
    private HttpSearchService HttpSearchService;

    @InjectMocks
    private HttpController httpController;

    @Test
    void search_ReturnSearchResponse() {
        HttpDocument document = HttpDocument.builder()
                .id("1")
                .uri("/api/orders")
                .method("GET")
                .statusCode("200")
                .timestamp(LocalDateTime.now())
                .build();

        SearchResponse<HttpDocument> mockResponse = SearchResponse.<HttpDocument>builder()
                .results(List.of(document))
                .totalHits(1L)
                .build();

        when(HttpSearchService.searchWithFullText(eq("orders"), eq("200"), eq(0), eq(20)))
                .thenReturn(mockResponse);

        ResponseEntity<SearchResponse<HttpDocument>> response = httpController.search(
                "orders", "200", 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResults()).hasSize(1);
        assertThat(response.getBody().getResults().get(0).getUri()).isEqualTo("/api/orders");
        assertThat(response.getBody().getTotalHits()).isEqualTo(1L);
    }

    @Test
    void getStats_ReturnStatsResponse() throws Exception {
        Map<String, Long> stats = Map.of("200", 1200L, "404", 23L, "500", 5L);
        StatsResponse mockResponse = StatsResponse.builder()
                .stats(stats)
                .build();

        when(HttpSearchService.getStats(eq("statusCode"), eq("INCOMING")))
                .thenReturn(mockResponse);

        ResponseEntity<StatsResponse> response = httpController.getStats("statusCode", "INCOMING");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStats()).hasSize(3);
        assertThat(response.getBody().getStats().get("200")).isEqualTo(1200L);
        assertThat(response.getBody().getStats().get("404")).isEqualTo(23L);
    }

    @Test
    void searchByFields_ReturnSearchResponse() {
        HttpDocument document = HttpDocument.builder()
                .id("1")
                .uri("/api/orders")
                .method("GET")
                .statusCode("200")
                .timestamp(LocalDateTime.now())
                .build();

        SearchResponse<HttpDocument> mockResponse = SearchResponse.<HttpDocument>builder()
                .results(List.of(document))
                .totalHits(1L)
                .build();

        when(HttpSearchService.searchByFields(eq("/api/orders"), eq("GET"), eq("200"), eq(0), eq(20)))
                .thenReturn(mockResponse);

        ResponseEntity<SearchResponse<HttpDocument>> response = httpController.searchByFields(
                "/api/orders", "GET", "200", 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResults()).hasSize(1);
        assertThat(response.getBody().getResults().get(0).getMethod()).isEqualTo("GET");
    }
}
