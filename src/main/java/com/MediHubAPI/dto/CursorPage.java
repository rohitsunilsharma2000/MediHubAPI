package com.MediHubAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorPage<T> {
    private List<T> content;
    private Long nextCursorId;
    private LocalDate nextCursorDate;
    private boolean hasMore;
}
