package com.tastyhouse.application.faq.port.out;

import java.time.LocalDateTime;

public record FaqManagementListItemResult(
    Long id,
    Long faqCategoryId,
    String question,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt
) {
}
