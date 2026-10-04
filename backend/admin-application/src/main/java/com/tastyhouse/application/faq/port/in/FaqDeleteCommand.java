package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record FaqDeleteCommand(Long faqId) {

    public FaqDeleteCommand {
        if (faqId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static FaqDeleteCommand of(Long faqId) {
        return new FaqDeleteCommand(faqId);
    }
}
