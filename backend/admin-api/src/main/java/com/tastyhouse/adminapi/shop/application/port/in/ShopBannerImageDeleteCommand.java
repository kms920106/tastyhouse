package com.tastyhouse.adminapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 배너 이미지 삭제 command. 요청 본문이 없는 연산이라 컨트롤러가 정적 팩토리로 조립한다. */
public record ShopBannerImageDeleteCommand(
    Long bannerImageId
) {
    public ShopBannerImageDeleteCommand {
        if (bannerImageId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBannerImageDeleteCommand of(Long bannerImageId) {
        return new ShopBannerImageDeleteCommand(bannerImageId);
    }
}
