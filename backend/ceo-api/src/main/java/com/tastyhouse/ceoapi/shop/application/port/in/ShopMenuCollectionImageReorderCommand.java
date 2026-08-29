package com.tastyhouse.ceoapi.shop.application.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴모음컷 표시 순서 교체 command(replace-all).
 */
public record ShopMenuCollectionImageReorderCommand(
    Long ceoId,
    Long shopId,
    List<Long> imageIds
) {
    public ShopMenuCollectionImageReorderCommand {
        if (ceoId == null || shopId == null || imageIds == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
