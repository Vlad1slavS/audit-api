package io.github.auditapi.model.elastic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse<T> {

    @Schema(description = "Список найденных документов")
    private List<T> results;

    @Schema(description = "Общее количество найденных документов")
    private long totalHits;

}
