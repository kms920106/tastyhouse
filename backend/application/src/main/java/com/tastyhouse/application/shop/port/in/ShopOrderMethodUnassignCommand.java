package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 주문수단 해제 command. 경로 변수뿐이라 컨트롤러가 정적 팩토리로 조립한다. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ShopOrderMethodUnassignCommand(
    Long shopId,
    String orderMethod
) {
    public ShopOrderMethodUnassignCommand {
        if (shopId == null || orderMethod == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopOrderMethodUnassignCommand of(Long shopId, String orderMethod) {
        return new ShopOrderMethodUnassignCommand(shopId, orderMethod);
    }
}
