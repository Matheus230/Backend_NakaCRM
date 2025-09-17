package com.nakacorp.backend.dto.res;

import org.springframework.data.domain.Page;
import java.util.List;

public record PageResponseDto<T>(
        List<T> content,
        int page,
        int size,
        int totalPages,
        long totalElements,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PageResponseDto<T> fromPage(Page<T> page) {
        return new PageResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
