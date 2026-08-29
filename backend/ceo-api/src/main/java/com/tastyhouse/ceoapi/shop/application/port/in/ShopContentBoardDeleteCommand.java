package com.tastyhouse.ceoapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 콘텐츠보드 삭제 command. 요청 본문이 없는 연산이므로 컨트롤러가 정적 팩토리로 조립한다.
 */
public record ShopContentBoardDeleteCommand(
    Long ceoId,
    Long shopId,
    Long contentBoardId
) {
    public ShopContentBoardDeleteCommand {
        if (ceoId == null || shopId == null || contentBoardId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopContentBoardDeleteCommand of(Long ceoId, Long shopId, Long contentBoardId) {
        return new ShopContentBoardDeleteCommand(ceoId, shopId, contentBoardId);
    }
}
