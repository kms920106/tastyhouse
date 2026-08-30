package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 태그 등록 command. */
public record TagCreateCommand(
    String tagName
) {
    public TagCreateCommand {
        if (tagName == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
