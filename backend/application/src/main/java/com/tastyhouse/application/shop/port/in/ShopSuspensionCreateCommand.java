package com.tastyhouse.application.shop.port.in;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 가게 영업 임시중지 등록 command.
 *
 * <p>{@code orderMethods}가 비어 있으면 전체 주문유형이 대상이라는 도메인 규약이므로 빈 목록·null을
 * 허용한다(가드는 식별자·기간에만 건다).
 */
public record ShopSuspensionCreateCommand(
    Long ceoId,
    Long shopId,
    String reason,
    List<String> orderMethods,
    LocalDateTime startAt,
    LocalDateTime endAt
) {
    public ShopSuspensionCreateCommand {
        if (ceoId == null || shopId == null || reason == null || startAt == null || endAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
