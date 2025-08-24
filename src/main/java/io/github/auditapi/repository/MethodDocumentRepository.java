package io.github.auditapi.repository;

import io.github.auditapi.model.elastic.MethodDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с документами методов
 */
@Repository
public interface MethodDocumentRepository extends ElasticsearchRepository<MethodDocument, String> {

    /**
     * Полнотекстовый поиск с фильтрацией по level
     */
    @Query("""
            { "bool": {
                "must": {
                    "multi_match": {
                        "query": "?0",
                        "fields": ["method^2", "args", "result"],
                        "type": "best_fields",
                        "analyzer": "audit_analyzer"
                    }
                },
                "filter": [
                    { "term": { "level": "?1" }}
                ]
            }}""")
    Page<MethodDocument> findByFullTextAndLevel(String query, String level, Pageable pageable);

    /**
     * Полнотекстовый поиск без фильтрации
     */
    @Query("""
            { "multi_match": {
                "query": "?0",
                "fields": ["method^2", "args", "result"],
                "type": "best_fields",
                "analyzer": "audit_analyzer"
            }}""")
    Page<MethodDocument> findByFullText(String query, Pageable pageable);

    Page<MethodDocument> findByMethodContainingAndLevel(String method, String level, Pageable pageable);

    Page<MethodDocument> findByMethodContaining(String method, Pageable pageable);

    Page<MethodDocument> findByLevel(String level, Pageable pageable);

    Page<MethodDocument> findByEventType(String eventType, Pageable pageable);

    Page<MethodDocument> findByMethodContainingAndLevelAndEventType(String method, String level, String eventType, Pageable pageable);

    @Query("""
            { "wildcard": { "method.keyword": "?0" }}""")
    Page<MethodDocument> findByMethodWildcard(String methodPattern, Pageable pageable);

    @Query("""
            { "bool": {
                "must": [
                    { "wildcard": { "method.keyword": "?0" }},
                    { "term": { "level": "?1" }}
                ]
            }}""")
    Page<MethodDocument> findByMethodWildcardAndLevel(String methodPattern, String level, Pageable pageable);

}
