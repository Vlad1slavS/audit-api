package io.github.auditapi.unit;

import io.github.auditapi.controller.v1.MethodController;
import io.github.auditapi.model.elastic.MethodDocument;
import io.github.auditapi.model.elastic.SearchResponse;
import io.github.auditapi.model.elastic.StatsResponse;
import io.github.auditapi.service.MethodSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MethodControllerTest {

    @Mock
    private MethodSearchService methodSearchService;

    @InjectMocks
    private MethodController methodController;

    @Test
    void search_ReturnSearchResponse() {
        MethodDocument document = MethodDocument.builder()
                .id("1")
                .method("getUserById")
                .level("INFO")
                .timestamp(LocalDateTime.now())
                .build();

        SearchResponse<MethodDocument> mockResponse = SearchResponse.<MethodDocument>builder()
                .results(List.of(document))
                .totalHits(1L)
                .build();

        when(methodSearchService.searchWithFullText(eq("getUserById"), eq("INFO"), eq(0), eq(20)))
                .thenReturn(mockResponse);

        ResponseEntity<SearchResponse<MethodDocument>> response = methodController.search(
                "getUserById", "INFO", 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResults()).hasSize(1);
        assertThat(response.getBody().getResults().getFirst().getMethod()).isEqualTo("getUserById");
        assertThat(response.getBody().getTotalHits()).isEqualTo(1L);
    }

    @Test
    void getStats_ReturnStatsResponse() throws Exception {
        Map<String, Long> stats = Map.of("INFO", 100L, "ERROR", 5L);
        StatsResponse mockResponse = StatsResponse.builder()
                .stats(stats)
                .build();

        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);

        when(methodSearchService.getStats(eq("level"), eq(from), eq(to)))
                .thenReturn(mockResponse);

        ResponseEntity<StatsResponse> response = methodController.getStats("level", from, to);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStats()).hasSize(2);
        assertThat(response.getBody().getStats().get("INFO")).isEqualTo(100L);
        assertThat(response.getBody().getStats().get("ERROR")).isEqualTo(5L);
    }

    @Test
    void searchByFields_ReturnSearchResponse() {
        MethodDocument document = MethodDocument.builder()
                .id("1")
                .method("Service.createUser")
                .level("ERROR")
                .eventType("EXECUTION")
                .timestamp(LocalDateTime.now())
                .build();

        SearchResponse<MethodDocument> mockResponse = SearchResponse.<MethodDocument>builder()
                .results(List.of(document))
                .totalHits(1L)
                .build();

        when(methodSearchService.searchByFields(eq("Service.*"), eq("ERROR"), eq("EXECUTION"), eq(0), eq(20)))
                .thenReturn(mockResponse);

        ResponseEntity<SearchResponse<MethodDocument>> response = methodController.searchByFields(
                "Service.*", "ERROR", "EXECUTION", 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResults()).hasSize(1);
        assertThat(response.getBody().getResults().getFirst().getLevel()).isEqualTo("ERROR");
    }

}
