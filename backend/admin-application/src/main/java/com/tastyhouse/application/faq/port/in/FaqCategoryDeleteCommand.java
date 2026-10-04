package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record FaqCategoryDeleteCommand(Long faqCategoryId) {

    public FaqCategoryDeleteCommand {
        if (faqCategoryId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static FaqCategoryDeleteCommand of(Long faqCategoryId) {
        return new FaqCategoryDeleteCommand(faqCategoryId);
    }
}
