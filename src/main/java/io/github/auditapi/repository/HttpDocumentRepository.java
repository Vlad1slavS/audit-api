package io.github.auditapi.repository;

import io.github.auditapi.model.elastic.HttpDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с Http документами
 */
@Repository
public interface HttpDocumentRepository extends ElasticsearchRepository<HttpDocument, String> {

    /**
     * Полнотекстовый поиск с фильтрацией по statusCode
     */
    @Query("""
            { "bool": {
                "must": {
                    "multi_match": {
                        "query": "?0",
                        "fields": ["uri^2", "requestBody", "responseBody"],
                        "type": "best_fields",
                        "analyzer": "audit_analyzer"
                    }
                },
                "filter": [
                    { "term": { "statusCode": "?1" }}
                ]
            }}""")
    Page<HttpDocument> findByFullTextAndStatusCode(String query, String statusCode, Pageable pageable);

    /**
     * Полнотекстовый поиск без фильтрации
     */
    @Query("""
            { "multi_match": {
                "query": "?0",
                "fields": ["uri^2", "requestBody", "responseBody"],
                "type": "best_fields",
                "analyzer": "audit_analyzer"
            }}""")
    Page<HttpDocument> findByFullText(String query, Pageable pageable);

    Page<HttpDocument> findByUriContaining(String uri, Pageable pageable);

    Page<HttpDocument> findByMethod(String method, Pageable pageable);

    Page<HttpDocument> findByStatusCode(String statusCode, Pageable pageable);

    Page<HttpDocument> findByUriContainingAndMethod(String uri, String method, Pageable pageable);

    Page<HttpDocument> findByUriContainingAndStatusCode(String uri, String statusCode, Pageable pageable);

    Page<HttpDocument> findByMethodAndStatusCode(String method, String statusCode, Pageable pageable);

    Page<HttpDocument> findByUriContainingAndMethodAndStatusCode(String uri, String method, String statusCode, Pageable pageable);

    @Query("""
            { "wildcard": { "uri.keyword": "?0" }}""")
    Page<HttpDocument> findByUriWildcard(String uriPattern, Pageable pageable);

    @Query("""
            { "bool": {
                "must": [
                    { "wildcard": { "uri.keyword": "?0" }},
                    { "term": { "method": "?1" }}
                ]
            }}""")
    Page<HttpDocument> findByUriWildcardAndMethod(String uriPattern, String method, Pageable pageable);

}
