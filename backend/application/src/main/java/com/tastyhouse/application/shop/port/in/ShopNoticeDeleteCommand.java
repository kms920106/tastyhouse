package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주 공지 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopNoticeDeleteCommand(
    Long ceoId,
    Long shopId,
    Long noticeId
) {
    public ShopNoticeDeleteCommand {
        if (ceoId == null || shopId == null || noticeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopNoticeDeleteCommand of(Long ceoId, Long shopId, Long noticeId) {
        return new ShopNoticeDeleteCommand(ceoId, shopId, noticeId);
    }
}
