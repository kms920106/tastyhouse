package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record FaqCategoryUpdateCommand(
    Long faqCategoryId,
    String name,
    Integer sort,
    boolean visible
) {

    public FaqCategoryUpdateCommand {
        if (faqCategoryId == null || name == null || sort == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
