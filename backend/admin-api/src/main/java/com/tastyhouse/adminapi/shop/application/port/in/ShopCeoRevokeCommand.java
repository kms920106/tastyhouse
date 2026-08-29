package com.tastyhouse.adminapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 담당 점주 해제 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ShopCeoRevokeCommand(
    Long adminId,
    Long shopId
) {
    public ShopCeoRevokeCommand {
        if (adminId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopCeoRevokeCommand of(Long adminId, Long shopId) {
        return new ShopCeoRevokeCommand(adminId, shopId);
    }
}
