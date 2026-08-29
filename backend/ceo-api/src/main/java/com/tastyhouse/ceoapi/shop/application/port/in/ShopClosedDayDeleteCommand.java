package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 정기 휴무 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopClosedDayDeleteCommand(
    Long ceoId,
    Long closedDayId
) {
    public ShopClosedDayDeleteCommand {
        if (ceoId == null || closedDayId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopClosedDayDeleteCommand of(Long ceoId, Long closedDayId) {
        return new ShopClosedDayDeleteCommand(ceoId, closedDayId);
    }
}
