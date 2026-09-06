package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record FaqCreateCommand(
    Long faqCategoryId,
    String question,
    String answer,
    Integer sort,
    boolean visible
) {
    public FaqCreateCommand {
        if (faqCategoryId == null || question == null || answer == null || sort == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
