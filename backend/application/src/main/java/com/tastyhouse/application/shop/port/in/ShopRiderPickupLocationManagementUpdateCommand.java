package com.tastyhouse.application.shop.port.in;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 라이더 픽업 위치 교정 command.
 *
 * <p>{@code adminId}는 액터 판정용이라 principal에서 주입한다 — 관리자 교정은 가게 변경이력에 남지 않는다.
 */
public record ShopRiderPickupLocationManagementUpdateCommand(
    Long shopId,
    Long adminId,
    String roadAddress,
    String lotAddress,
    String detailAddress,
    BigDecimal latitude,
    BigDecimal longitude
) {
    public ShopRiderPickupLocationManagementUpdateCommand {
        if (shopId == null || adminId == null || roadAddress == null
            || latitude == null || longitude == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
