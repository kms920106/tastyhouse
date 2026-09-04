package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 브레이크타임 삭제 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ShopBreakTimeManagementDeleteCommand(
    Long adminId,
    Long breakTimeId
) {
    public ShopBreakTimeManagementDeleteCommand {
        if (adminId == null || breakTimeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBreakTimeManagementDeleteCommand of(Long adminId, Long breakTimeId) {
        return new ShopBreakTimeManagementDeleteCommand(adminId, breakTimeId);
    }
}
