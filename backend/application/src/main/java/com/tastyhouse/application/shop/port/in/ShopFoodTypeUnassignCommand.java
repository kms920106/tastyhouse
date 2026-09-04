package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 음식종류 해제 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ShopFoodTypeUnassignCommand(
    Long shopId,
    Long foodTypeCategoryId
) {
    public ShopFoodTypeUnassignCommand {
        if (shopId == null || foodTypeCategoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopFoodTypeUnassignCommand of(Long shopId, Long foodTypeCategoryId) {
        return new ShopFoodTypeUnassignCommand(shopId, foodTypeCategoryId);
    }
}
