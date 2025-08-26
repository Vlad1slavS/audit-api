package io.github.auditapi.model.elastic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;

@Data
@Builder
@Document(indexName = "audit-requests")
@Schema(description = "Документ HTTP-запроса")
public class HttpDocument {

    @Id
    @Schema(description = "Уникальный идентификатор документа", example = "507f1f77bcf86cd799439011")
    private String id;

    @Schema(description = "Временная метка запроса")
    private LocalDateTime timestamp;

    @Schema(description = "URI запроса", example = "/api/v1/users/123")
    private String uri;

    @Schema(description = "HTTP метод", example = "GET")
    private String method;

    @Schema(description = "Направление запроса", example = "INCOMING", allowableValues = {"INCOMING", "OUTGOING"})
    private String direction;

    @Schema(description = "HTTP статус-код ответа", example = "200")
    private String statusCode;

    @Schema(description = "Тело HTTP-запроса", example = "{\"name\":\"John\",\"email\":\"john@example.com\"}")
    private String requestBody;

    @Schema(description = "Тело HTTP-ответа", example = "{\"id\":123,\"name\":\"John\",\"email\":\"john@example.com\"}")
    private String responseBody;

}
