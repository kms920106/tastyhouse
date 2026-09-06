package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record FaqCategoryDeleteCommand(Long faqCategoryId) {
    public FaqCategoryDeleteCommand {
        if (faqCategoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static FaqCategoryDeleteCommand of(Long faqCategoryId) {
        return new FaqCategoryDeleteCommand(faqCategoryId);
    }
}
