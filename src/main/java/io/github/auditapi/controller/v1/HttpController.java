package io.github.auditapi.controller.v1;

import io.github.auditapi.model.elastic.HttpDocument;
import io.github.auditapi.model.elastic.SearchResponse;
import io.github.auditapi.model.elastic.StatsResponse;
import io.github.auditapi.service.HttpSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
@Tag(name = "HTTP Requests Controller", description = "API для работы с документами HTTP-запросов")
public class HttpController {

    private final HttpSearchService httpSearchService;

    @Operation(
            summary = "Полнотекстовый поиск HTTP-запросов",
            description = "Выполняет полнотекстовый поиск по HTTP-запросам с возможностью фильтрации по статус-коду"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Поиск выполнен успешно",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SearchResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры запроса"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @GetMapping("/search")
    public ResponseEntity<SearchResponse<HttpDocument>> search(
            @Parameter(description = "Поисковый запрос для полнотекстового поиска", example = "orders")
            @RequestParam(required = false) String query,

            @Parameter(description = "Фильтр по HTTP статус-коду", example = "200")
            @RequestParam(required = false) String statusCode,

            @Parameter(description = "Номер страницы", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        SearchResponse<HttpDocument> response = httpSearchService.searchWithFullText(
                query, statusCode, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Получение статистики HTTP-запросов",
            description = "Возвращает агрегированную статистику по HTTP-запросам, сгруппированную по указанному полю"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Статистика получена успешно",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StatsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры запроса"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Ошибка при получении статистики"
            )
    })
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats(
            @Parameter(
                    description = "Поле для группировки статистики",
                    example = "statusCode",
                    schema = @Schema(allowableValues = {"statusCode", "method", "uri"})
            )
            @RequestParam(defaultValue = "statusCode") String groupBy,

            @Parameter(
                    description = "Фильтр по направлению запроса",
                    example = "INCOMING",
                    schema = @Schema(allowableValues = {"INCOMING", "OUTGOING"})
            )
            @RequestParam(required = false) String direction) throws IOException {

        StatsResponse response = httpSearchService.getStats(groupBy, direction);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Поиск HTTP-запросов по полям",
            description = "Выполняет поиск HTTP-запросов по конкретным полям"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Поиск выполнен успешно",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SearchResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры запроса"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @GetMapping
    public ResponseEntity<SearchResponse<HttpDocument>> searchByFields(
            @Parameter(
                    description = "URI запроса",
                    example = "/api/orders"
            )
            @RequestParam(required = false) String uri,

            @Parameter(
                    description = "HTTP метод",
                    example = "GET"
            )
            @RequestParam(required = false) String method,

            @Parameter(description = "HTTP статус-код", example = "200")
            @RequestParam(required = false) String statusCode,

            @Parameter(description = "Номер страницы", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        SearchResponse<HttpDocument> response = httpSearchService.searchByFields(
                uri, method, statusCode, page, size);
        return ResponseEntity.ok(response);
    }

}

