package com.tastyhouse.application.shop.port.in;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 라이더 픽업 위치 등록·수정 command.
 *
 * <p>지번주소·상세주소는 선택 입력이라 null 가드를 걸지 않는다.
 */
public record ShopRiderPickupLocationOwnerUpdateCommand(
    Long ceoId,
    Long shopId,
    String roadAddress,
    String lotAddress,
    String detailAddress,
    BigDecimal latitude,
    BigDecimal longitude
) {
    public ShopRiderPickupLocationOwnerUpdateCommand {
        if (ceoId == null || shopId == null || roadAddress == null || latitude == null || longitude == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
