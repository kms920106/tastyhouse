package com.tastyhouse.ceoapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 대표이미지 변경 요청 command. 이미지 파일은 별도 {@code MultipartFile} 파라미터로 전달된다.
 */
public record ShopThumbnailChangeRequestCommand(
    Long ceoId,
    Long shopId
) {
    public ShopThumbnailChangeRequestCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopThumbnailChangeRequestCommand of(Long ceoId, Long shopId) {
        return new ShopThumbnailChangeRequestCommand(ceoId, shopId);
    }
}
