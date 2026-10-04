package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

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
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
