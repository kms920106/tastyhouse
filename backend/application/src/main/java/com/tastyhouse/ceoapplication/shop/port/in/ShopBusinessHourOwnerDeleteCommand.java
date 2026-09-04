package com.tastyhouse.ceoapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 운영시간 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopBusinessHourOwnerDeleteCommand(
    Long ceoId,
    Long businessHourId
) {
    public ShopBusinessHourOwnerDeleteCommand {
        if (ceoId == null || businessHourId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBusinessHourOwnerDeleteCommand of(Long ceoId, Long businessHourId) {
        return new ShopBusinessHourOwnerDeleteCommand(ceoId, businessHourId);
    }
}
