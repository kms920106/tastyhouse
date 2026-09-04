package com.tastyhouse.ceoapplication.shop.port.in;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 여러 가게에 같은 영업 임시중지를 일괄 등록하는 command.
 *
 * <p>단건 등록과 달리 대상이 {@code shopIds} 목록이다 — 경로 변수 {@code shopId}가 없는 엔드포인트라
 * 대상 가게가 본문에 실린다.
 */
public record ShopSuspensionBulkCreateCommand(
    Long ceoId,
    List<Long> shopIds,
    String reason,
    List<String> orderMethods,
    LocalDateTime startAt,
    LocalDateTime endAt
) {
    public ShopSuspensionBulkCreateCommand {
        if (ceoId == null || shopIds == null || reason == null || startAt == null || endAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
