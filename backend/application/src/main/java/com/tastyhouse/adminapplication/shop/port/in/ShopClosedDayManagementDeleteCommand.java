package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 정기 휴무일 삭제 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ShopClosedDayManagementDeleteCommand(
    Long adminId,
    Long closedDayId
) {
    public ShopClosedDayManagementDeleteCommand {
        if (adminId == null || closedDayId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopClosedDayManagementDeleteCommand of(Long adminId, Long closedDayId) {
        return new ShopClosedDayManagementDeleteCommand(adminId, closedDayId);
    }
}
