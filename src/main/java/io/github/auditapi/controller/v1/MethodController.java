package io.github.auditapi.controller.v1;

import io.github.auditapi.model.elastic.MethodDocument;
import io.github.auditapi.model.elastic.SearchResponse;
import io.github.auditapi.model.elastic.StatsResponse;
import io.github.auditapi.service.MethodSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/methods")
@RequiredArgsConstructor
@Tag(name = "Method Controller", description = "API для работы с документами методов")
public class MethodController {

    private final MethodSearchService methodSearchService;

    @Operation(
            summary = "Полнотекстовый поиск вызовов методов",
            description = "Выполняет полнотекстовый поиск по вызовам методов с возможностью фильтрации по уровню логирования"
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
    public ResponseEntity<SearchResponse<MethodDocument>> search(
            @Parameter(description = "Поисковый запрос для полнотекстового поиска", example = "UserService")
            @RequestParam(required = false) String query,

            @Parameter(
                    description = "Фильтр по уровню логирования",
                    example = "INFO",
                    schema = @Schema(allowableValues = {"DEBUG", "INFO", "WARN", "ERROR"})
            )
            @RequestParam(required = false) String level,

            @Parameter(description = "Номер страницы", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        SearchResponse<MethodDocument> response = methodSearchService.searchWithFullText(query, level, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Получение статистики вызовов методов",
            description = "Возвращает агрегированную статистику по вызовам методов с возможностью фильтрации по датам"
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
                    example = "level",
                    schema = @Schema(allowableValues = {"level", "method"})
            )
            @RequestParam(defaultValue = "level") String groupBy,

            @Parameter(
                    description = "Начальная дата фильтрации",
                    example = "2025-01-01"
            )
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(
                    description = "Конечная дата фильтрации",
                    example = "2025-01-31"
            )
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) throws IOException {

        StatsResponse response = methodSearchService.getStats(groupBy, from, to);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Поиск вызовов методов по полям",
            description = "Выполняет точный поиск вызовов методов по конкретным полям"
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
    public ResponseEntity<SearchResponse<MethodDocument>> searchByFields(
            @Parameter(
                    description = "Имя метода",
                    example = "UserService.findById"
            )
            @RequestParam(required = false) String method,

            @Parameter(
                    description = "Уровень логирования",
                    example = "INFO",
                    schema = @Schema(allowableValues = {"DEBUG", "INFO", "WARN", "ERROR"})
            )
            @RequestParam(required = false) String logLevel,

            @Parameter(
                    description = "Тип события",
                    example = "START",
                    schema = @Schema(allowableValues = {"START", "END", "ERROR"})
            )
            @RequestParam(required = false) String eventType,

            @Parameter(description = "Номер страницы", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        SearchResponse<MethodDocument> response = methodSearchService.searchByFields(
                method, logLevel, eventType, page, size);
        return ResponseEntity.ok(response);
    }
}
