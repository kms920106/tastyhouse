package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 폐업 처리 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ShopCloseCommand(
    Long shopId
) {
    public ShopCloseCommand {
        if (shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopCloseCommand of(Long shopId) {
        return new ShopCloseCommand(shopId);
    }
}
