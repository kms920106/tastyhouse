package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record FaqCategoryCreateCommand(
    String name,
    Integer sort,
    boolean visible
) {
    public FaqCategoryCreateCommand {
        if (name == null || sort == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
