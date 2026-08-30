package com.tastyhouse.ceoapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴모음컷 등록 command. 이미지 파일은 별도 {@code MultipartFile} 파라미터로 전달된다.
 */
public record ShopMenuCollectionImageCreateCommand(
    Long ceoId,
    Long shopId
) {
    public ShopMenuCollectionImageCreateCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopMenuCollectionImageCreateCommand of(Long ceoId, Long shopId) {
        return new ShopMenuCollectionImageCreateCommand(ceoId, shopId);
    }
}
