package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record FaqCreateCommand(
    Long faqCategoryId,
    String question,
    String answer,
    Integer sort,
    boolean visible
) {

    public FaqCreateCommand {
        if (faqCategoryId == null || question == null || answer == null || sort == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
