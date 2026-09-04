package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 테하 초이스 수정 command. */
public record ShopChoiceUpdateCommand(
    Long choiceId,
    String title,
    String content
) {
    public ShopChoiceUpdateCommand {
        if (choiceId == null || title == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
