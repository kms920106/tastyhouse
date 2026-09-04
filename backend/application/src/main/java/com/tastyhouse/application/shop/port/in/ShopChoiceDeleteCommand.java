package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 테하 초이스 삭제 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ShopChoiceDeleteCommand(
    Long choiceId
) {
    public ShopChoiceDeleteCommand {
        if (choiceId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopChoiceDeleteCommand of(Long choiceId) {
        return new ShopChoiceDeleteCommand(choiceId);
    }
}
