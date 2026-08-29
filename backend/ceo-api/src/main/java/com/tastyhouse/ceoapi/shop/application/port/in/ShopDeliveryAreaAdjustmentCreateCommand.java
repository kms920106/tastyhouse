package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 배달지역 조정 신청 접수 command.
 *
 * <p>동의서 파일은 이 record가 아니라 서비스 메서드의 별도 {@code MultipartFile} 파라미터로 전달된다.
 */
public record ShopDeliveryAreaAdjustmentCreateCommand(
    Long ceoId,
    Long shopId,
    String counterpartShopName,
    String counterpartBusinessNumber,
    String franchiseName,
    String reason
) {
    public ShopDeliveryAreaAdjustmentCreateCommand {
        if (ceoId == null || shopId == null || counterpartShopName == null
            || counterpartBusinessNumber == null || franchiseName == null || reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
