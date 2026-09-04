package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴모음컷 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopMenuCollectionImageDeleteCommand(
    Long ceoId,
    Long shopId,
    Long imageId
) {
    public ShopMenuCollectionImageDeleteCommand {
        if (ceoId == null || shopId == null || imageId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopMenuCollectionImageDeleteCommand of(Long ceoId, Long shopId, Long imageId) {
        return new ShopMenuCollectionImageDeleteCommand(ceoId, shopId, imageId);
    }
}
