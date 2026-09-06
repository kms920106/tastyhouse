package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record FaqUpdateCommand(
    Long faqId,
    Long faqCategoryId,
    String question,
    String answer,
    Integer sort,
    boolean visible
) {
    public FaqUpdateCommand {
        if (faqId == null || faqCategoryId == null || question == null || answer == null || sort == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
