package com.tastyhouse.core.domain.faq.application.dto.command;

import com.tastyhouse.core.domain.faq.domain.vo.FaqCategoryId;

public record FaqUpdateCommand(
    FaqCategoryId faqCategoryId,
    String question,
    String answer,
    Integer sort,
    boolean visible
) {

    public static FaqUpdateCommand of(FaqCategoryId faqCategoryId, String question, String answer, Integer sort, boolean visible) {
        return new FaqUpdateCommand(faqCategoryId, question, answer, sort, visible);
    }
}
