package com.tastyhouse.application.faq.port.out;

import java.time.LocalDateTime;

public record FaqCategoryManagementResult(
    Long id,
    String name,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt
) {
}
