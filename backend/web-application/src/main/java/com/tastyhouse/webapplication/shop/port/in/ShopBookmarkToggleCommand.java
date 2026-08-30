package com.tastyhouse.webapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 즐겨찾기 토글 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopBookmarkToggleCommand(
    Long memberId,
    Long shopId
) {
    public ShopBookmarkToggleCommand {
        if (memberId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopBookmarkToggleCommand of(Long memberId, Long shopId) {
        return new ShopBookmarkToggleCommand(memberId, shopId);
    }
}
