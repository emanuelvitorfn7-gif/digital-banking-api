package com.digitalbanking.api.dto.comum;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(name = "PageResponse", description = "Envelope genérico de resposta paginada.")
public record PageResponse<T>(
        @Schema(description = "Itens da página atual.")
        List<T> content,

        @Schema(description = "Número da página atual (base zero).", example = "0")
        int page,

        @Schema(description = "Quantidade de itens por página.", example = "10")
        int size,

        @Schema(description = "Total de itens disponíveis.", example = "2")
        long totalElements,

        @Schema(description = "Total de páginas disponíveis.", example = "1")
        int totalPages
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

}
