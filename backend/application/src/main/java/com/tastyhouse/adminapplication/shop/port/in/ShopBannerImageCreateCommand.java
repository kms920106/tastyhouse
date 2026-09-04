package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 배너 이미지 등록 command. */
public record ShopBannerImageCreateCommand(
    Long shopId,
    Long imageFileId,
    Integer sort
) {
    public ShopBannerImageCreateCommand {
        if (shopId == null || imageFileId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
