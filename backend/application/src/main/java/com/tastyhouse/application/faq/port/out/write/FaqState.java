package com.tastyhouse.application.faq.port.out.write;

import java.time.LocalDateTime;

public record FaqState(
    Long id,
    Long faqCategoryId,
    String question,
    String answer,
    Integer sort,
    boolean visible,
    boolean deleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
