package com.movietracker.watchlog.dto.response;

import java.util.List;

/**
 * Generic pagination response wrapper matching Movie Tracker platform standard.
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / (double) size);
        boolean isLast = page >= totalPages || totalPages <= 1;
        return new PagedResponse<>(content, page, size, totalElements, totalPages, isLast);
    }
}
