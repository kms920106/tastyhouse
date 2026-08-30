package com.tastyhouse.adminapplication.shop.port.in;

import java.math.BigDecimal;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 등록 command. {@code adminId}는 접근권한 부여 이력의 조치자라 principal에서 주입한다. */
public record ShopCreateCommand(
    Long adminId,
    Long ceoId,
    Long stationId,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    String roadAddress,
    String lotAddress,
    String phoneNumber,
    Long thumbnailImageFileId
) {
    public ShopCreateCommand {
        if (adminId == null || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
