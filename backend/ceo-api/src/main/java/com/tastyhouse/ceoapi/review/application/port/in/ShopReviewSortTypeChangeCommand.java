package com.tastyhouse.ceoapi.review.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 리뷰 정렬 설정 저장 command. {@code sortType}은 경계 타입인 문자열로 받고 enum 승격은 서비스가 한다.
 */
public record ShopReviewSortTypeChangeCommand(
    Long ceoId,
    Long shopId,
    String sortType
) {
    public ShopReviewSortTypeChangeCommand {
        if (ceoId == null || shopId == null || sortType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
