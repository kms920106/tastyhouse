package com.tastyhouse.application.faq.port.out;

import java.time.LocalDateTime;

public record FaqDetailResult(
    Long id,
    Long faqCategoryId,
    String question,
    String answer,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
