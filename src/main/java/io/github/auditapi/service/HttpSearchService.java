package io.github.auditapi.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import io.github.auditapi.model.elastic.HttpDocument;
import io.github.auditapi.model.elastic.StatsResponse;
import io.github.auditapi.repository.HttpDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для работы с Http документами
 */
@Service
@RequiredArgsConstructor
public class HttpSearchService {

    private final HttpDocumentRepository repository;
    private final ElasticsearchClient elasticsearchClient;

    public io.github.auditapi.model.elastic.SearchResponse<HttpDocument> searchWithFullText(
            String query, String statusCode, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<HttpDocument> results;

        if (query != null && !query.isBlank() && statusCode != null && !statusCode.isBlank()) {
            results = repository.findByFullTextAndStatusCode(query, statusCode, pageable);
        } else if (query != null && !query.isBlank()) {
            results = repository.findByFullText(query, pageable);
        } else if (statusCode != null && !statusCode.isBlank()) {
            results = repository.findByStatusCode(statusCode, pageable);
        } else {
            results = repository.findAll(pageable);
        }

        return new io.github.auditapi.model.elastic.SearchResponse<>(
                results.getContent(),
                results.getTotalElements()
        );
    }

    public io.github.auditapi.model.elastic.SearchResponse<HttpDocument> searchByFields(
            String uri, String method, String statusCode, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<HttpDocument> results;

        boolean isWildcardUri = uri != null && (uri.contains("*") || uri.contains("?"));

        if (uri != null && method != null && statusCode != null) {
            results = repository.findByUriContainingAndMethodAndStatusCode(uri, method, statusCode, pageable);
        } else if (uri != null && method != null) {
            if (isWildcardUri) {
                results = repository.findByUriWildcardAndMethod(uri, method, pageable);
            } else {
                results = repository.findByUriContainingAndMethod(uri, method, pageable);
            }
        } else if (uri != null && statusCode != null) {
            results = repository.findByUriContainingAndStatusCode(uri, statusCode, pageable);
        } else if (method != null && statusCode != null) {
            results = repository.findByMethodAndStatusCode(method, statusCode, pageable);
        } else if (uri != null) {
            if (isWildcardUri) {
                results = repository.findByUriWildcard(uri, pageable);
            } else {
                results = repository.findByUriContaining(uri, pageable);
            }
        } else if (method != null) {
            results = repository.findByMethod(method, pageable);
        } else if (statusCode != null) {
            results = repository.findByStatusCode(statusCode, pageable);
        } else {
            results = repository.findAll(pageable);
        }

        return new io.github.auditapi.model.elastic.SearchResponse<>(
                results.getContent(),
                results.getTotalElements()
        );
    }

    public io.github.auditapi.model.elastic.StatsResponse getStats(String groupBy, String direction) throws IOException {

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        String aggregationField;


        if (direction != null && !direction.isBlank()) {
            boolQuery.filter(f -> f.term(t -> t.field("direction").value(direction)));
        }

        switch (groupBy) {
            case "statusCode":
                aggregationField = "statusCode";
                break;
            case "method":
                aggregationField = "method";
                break;
            case "uri":
                aggregationField = "uri.keyword";
                break;
            default:
                aggregationField = "statusCode";
        }

        String aggregationName = aggregationField + "_stats";

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("audit-requests")
                .query(q -> q.bool(boolQuery.build()))
                .size(0)
                .aggregations(aggregationName, Aggregation.of(a -> a
                        .terms(t -> t.field(aggregationField).size(100))
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

        return StatsResponse.builder().stats(stats).build();
    }

}
