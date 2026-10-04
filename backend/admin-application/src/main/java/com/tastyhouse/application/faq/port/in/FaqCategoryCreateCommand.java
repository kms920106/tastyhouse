package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record FaqCategoryCreateCommand(
    String name,
    Integer sort,
    boolean visible
) {

    public FaqCategoryCreateCommand {
        if (name == null || sort == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
