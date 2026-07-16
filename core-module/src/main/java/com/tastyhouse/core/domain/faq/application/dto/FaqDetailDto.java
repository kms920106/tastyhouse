package com.tastyhouse.core.domain.faq.application.dto;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.faq.domain.vo.FaqId;

public record FaqDetailDto(
    FaqId faqId,
    Long faqCategoryId,
    String question,
    String answer,
    Integer sort,
    boolean visible,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static FaqDetailDto from(
        FaqId faqId,
        Long faqCategoryId,
        String question,
        String answer,
        Integer sort,
        boolean visible,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new FaqDetailDto(faqId, faqCategoryId, question, answer, sort, visible, createdAt, updatedAt);
    }
}
