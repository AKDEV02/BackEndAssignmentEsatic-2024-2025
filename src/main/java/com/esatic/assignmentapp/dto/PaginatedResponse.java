package com.esatic.assignmentapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> docs;
    private long totalDocs;
    private int limit;
    private int page;
    private int totalPages;
    private int pagingCounter;
    private boolean hasPrevPage;
    private boolean hasNextPage;
    private Integer prevPage;
    private Integer nextPage;
}
