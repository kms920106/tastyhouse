package com.tastyhouse.infrastructure.shop.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.RiderGuideActionType;
import com.tastyhouse.domain.shop.model.RiderGuideActorType;

/**
 * 라이더 안내 변경 이력 조회 결과(관리자 검수 화면용).
 */
public record ShopRiderGuideHistoryResult(
    Long id,
    RiderGuideActorType actorType,
    Long actorId,
    RiderGuideActionType actionType,
    String previousVisitGuide,
    String newVisitGuide,
    String reason,
    LocalDateTime createdAt
) {

}
