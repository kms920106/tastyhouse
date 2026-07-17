package com.tastyhouse.core.domain.faq.application.dto.result;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.faq.domain.vo.FaqId;

public record FaqDetailResult(
    FaqId faqId,
    Long faqCategoryId,
    String question,
    String answer,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static FaqDetailResult from(
        FaqId faqId,
        Long faqCategoryId,
        String question,
        String answer,
        Integer sort,
        boolean visible,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new FaqDetailResult(faqId, faqCategoryId, question, answer, sort, visible, createdAt, updatedAt);
    }
}
