package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record FaqDeleteCommand(Long faqId) {
    public FaqDeleteCommand {
        if (faqId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static FaqDeleteCommand of(Long faqId) {
        return new FaqDeleteCommand(faqId);
    }
}
