package com.tastyhouse.ceoapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 정기 휴무 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopClosedDayOwnerDeleteCommand(
    Long ceoId,
    Long closedDayId
) {
    public ShopClosedDayOwnerDeleteCommand {
        if (ceoId == null || closedDayId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopClosedDayOwnerDeleteCommand of(Long ceoId, Long closedDayId) {
        return new ShopClosedDayOwnerDeleteCommand(ceoId, closedDayId);
    }
}
