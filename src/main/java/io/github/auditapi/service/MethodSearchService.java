package io.github.auditapi.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import io.github.auditapi.model.elastic.MethodDocument;
import io.github.auditapi.model.elastic.StatsResponse;
import io.github.auditapi.repository.MethodDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для работы с документами методов
 */
@Service
@RequiredArgsConstructor
public class MethodSearchService {

    private final MethodDocumentRepository repository;
    private final ElasticsearchClient elasticsearchClient;

    public io.github.auditapi.model.elastic.SearchResponse<MethodDocument> searchWithFullText(
            String query, String level, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<MethodDocument> results;

        if (query != null && !query.isBlank() && level != null && !level.isBlank()) {
            results = repository.findByFullTextAndLevel(query, level, pageable);
        } else if (query != null && !query.isBlank()) {
            results = repository.findByFullText(query, pageable);
        } else if (level != null && !level.isBlank()) {
            results = repository.findByLevel(level, pageable);
        } else {
            results = repository.findAll(pageable);
        }

        return new io.github.auditapi.model.elastic.SearchResponse<>(
                results.getContent(),
                results.getTotalElements()
        );

    }

    public io.github.auditapi.model.elastic.SearchResponse<MethodDocument> searchByFields(
            String method, String level, String eventType, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<MethodDocument> results;

        boolean isWildcardMethod = method != null && (method.contains("*") || method.contains("?"));

        if (method != null && level != null && eventType != null) {
            results = repository.findByMethodContainingAndLevelAndEventType(method, level, eventType, pageable);
        } else if (method != null && level != null) {
            if (isWildcardMethod) {
                results = repository.findByMethodWildcardAndLevel(method, level, pageable);
            } else {
                results = repository.findByMethodContainingAndLevel(method, level, pageable);
            }
        } else if (method != null) {
            if (isWildcardMethod) {
                results = repository.findByMethodWildcard(method, pageable);
            } else {
                results = repository.findByMethodContaining(method, pageable);
            }
        } else if (level != null) {
            results = repository.findByLevel(level, pageable);
        } else if (eventType != null) {
            results = repository.findByEventType(eventType, pageable);
        } else {
            results = repository.findAll(pageable);
        }

        return new io.github.auditapi.model.elastic.SearchResponse<>(
                results.getContent(),
                results.getTotalElements()
        );
    }

    public StatsResponse getStats(String groupBy, LocalDate from, LocalDate to) throws IOException {

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        if (from != null && to != null) {
            boolQuery.filter(f -> f.range(RangeQuery.of(r -> r
                    .date(d -> d
                            .field("timestamp")
                            .gte(from.toString())
                            .lte(to.toString())
                    )
            )));
        } else if (from != null) {
            boolQuery.filter(f -> f.range(RangeQuery.of(r -> r
                    .date(d -> d
                            .field("timestamp")
                            .gte(from.toString())
                    )
            )));
        } else if (to != null) {
            boolQuery.filter(f -> f.range(RangeQuery.of(r -> r
                    .date(d -> d
                            .field("timestamp")
                            .lte(to.toString())
                    )
            )));
        }

        String aggregationField = "level".equals(groupBy) ? "level" : "method.keyword";
        String aggregationName = aggregationField + "_stats";

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("audit-methods")
                .query(q -> q.bool(boolQuery.build()))
                .size(0)
                .aggregations(aggregationName, Aggregation.of(a -> a
                        .terms(t -> t.field(aggregationField))
                ))
        );

        SearchResponse<Void> response = elasticsearchClient.search(searchRequest, Void.class);

        Map<String, Long> stats = new HashMap<>();
        StringTermsAggregate aggregate = response.aggregations()
                .get(aggregationName)
                .sterms();

        for (StringTermsBucket bucket : aggregate.buckets().array()) {
            stats.put(bucket.key().stringValue(), bucket.docCount());
        }

        return StatsResponse.builder()
                .stats(stats)
                .build();
    }

}

