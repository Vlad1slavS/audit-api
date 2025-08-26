package io.github.auditapi.model.elastic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class StatsResponse {

    @Schema(
            description = "Map статистика, где ключ - значение поля, значение - количество документов",
            example = "{\"200\": 1250, \"404\": 85, \"500\": 12}"
    )
    private Map<String, Long> stats;

}
