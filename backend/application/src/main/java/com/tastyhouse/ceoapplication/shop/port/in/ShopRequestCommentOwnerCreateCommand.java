package com.tastyhouse.ceoapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주 가게 요청 문의 등록 command.
 */
public record ShopRequestCommentOwnerCreateCommand(
    Long ceoId,
    Long shopId,
    Long requestId,
    String content
) {
    public ShopRequestCommentOwnerCreateCommand {
        if (ceoId == null || shopId == null || requestId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
