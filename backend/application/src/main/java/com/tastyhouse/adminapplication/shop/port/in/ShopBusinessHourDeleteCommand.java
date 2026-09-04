package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 운영시간 삭제 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ShopBusinessHourDeleteCommand(
    Long adminId,
    Long businessHourId
) {
    public ShopBusinessHourDeleteCommand {
        if (adminId == null || businessHourId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBusinessHourDeleteCommand of(Long adminId, Long businessHourId) {
        return new ShopBusinessHourDeleteCommand(adminId, businessHourId);
    }
}
