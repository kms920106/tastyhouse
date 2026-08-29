package com.tastyhouse.adminapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 편의시설 해제 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ShopAmenityUnassignCommand(
    Long adminId,
    Long shopId,
    Long amenityCategoryId
) {
    public ShopAmenityUnassignCommand {
        if (adminId == null || shopId == null || amenityCategoryId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopAmenityUnassignCommand of(Long adminId, Long shopId, Long amenityCategoryId) {
        return new ShopAmenityUnassignCommand(adminId, shopId, amenityCategoryId);
    }
}
