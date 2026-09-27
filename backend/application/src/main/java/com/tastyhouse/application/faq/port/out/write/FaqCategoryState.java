package com.tastyhouse.application.faq.port.out.write;

import java.time.LocalDateTime;

public record FaqCategoryState(
    Long id,
    String name,
    Integer sort,
    boolean visible,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
