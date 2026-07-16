package com.tastyhouse.core.domain.faq.application.dto.command;

import com.tastyhouse.core.domain.faq.domain.vo.FaqCategoryId;

public record FaqCreateCommand(
    FaqCategoryId faqCategoryId,
    String question,
    String answer,
    Integer sort,
    boolean visible
) {

    public static FaqCreateCommand of(FaqCategoryId faqCategoryId, String question, String answer, Integer sort, boolean visible) {
        return new FaqCreateCommand(faqCategoryId, question, answer, sort, visible);
    }
}
