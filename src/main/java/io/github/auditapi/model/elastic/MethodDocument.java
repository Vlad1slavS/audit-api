package io.github.auditapi.model.elastic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "audit-methods")
@Schema(description = "Документ метода")
public class MethodDocument {

    @Id
    @Schema(description = "Уникальный идентификатор документа")
    private String id;

    @Schema(description = "Временная метка вызова метода")
    private LocalDateTime timestamp;

    @Schema(description = "Имя вызванного метода", example = "UserService.findById")
    private String method;

    @Schema(description = "Уровень логирования", example = "INFO")
    private String level;

    @Schema(description = "Тип события", example = "START", allowableValues = {"START", "END", "ERROR"})
    private String eventType;

    @Schema(description = "Идентификатор для связывания событий")
    private String correlationId;

    @Schema(description = "Аргументы метода в JSON формате", example = "[123, \"active\"]")
    private String args;

    @Schema(description = "Результат выполнения метода в JSON формате", example = "{\"id\":123,\"name\":\"John\",\"status\":\"active\"}")
    private String result;

    @Schema(description = "Сообщение об ошибке (если произошла)", example = "User not found with id: 123")
    private String errorMessage;

}
