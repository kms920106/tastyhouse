package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 주문안내 등록·수정 command(가게당 1건 전체교체).
 */
public record ShopOrderNoticeUpsertCommand(
    Long ceoId,
    Long shopId,
    String content
) {
    public ShopOrderNoticeUpsertCommand {
        if (ceoId == null || shopId == null || content == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
