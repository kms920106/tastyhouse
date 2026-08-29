package com.tastyhouse.adminapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 브레이크타임 삭제 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ShopBreakTimeDeleteCommand(
    Long adminId,
    Long breakTimeId
) {
    public ShopBreakTimeDeleteCommand {
        if (adminId == null || breakTimeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBreakTimeDeleteCommand of(Long adminId, Long breakTimeId) {
        return new ShopBreakTimeDeleteCommand(adminId, breakTimeId);
    }
}
