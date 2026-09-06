package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record FaqCategoryUpdateCommand(
    Long faqCategoryId,
    String name,
    Integer sort,
    boolean visible
) {
    public FaqCategoryUpdateCommand {
        if (faqCategoryId == null || name == null || sort == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
