package com.tastyhouse.infrastructure.shop.query;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeType;

/**
 * 가게 변경이력 목록 항목 조회 결과.
 *
 * <p>반드시 {@code public}이어야 한다 — package-private이면 QueryDSL {@code Projections.constructor}가
 * {@code Class#getConstructors()}(public 생성자만 반환)에서 생성자를 찾지 못해 <b>컴파일은 통과하고 그 쿼리
 * 실행 시에만 500</b>이 난다. {@code QueryResultRecordVisibilityTest}가 이를 가드한다.
 *
 * <p>{@code actorType}/{@code actorId}는 투영하지 않는다 — 점주 화면은 자기 가게 이력만 보므로 행위자
 * 정보가 필요 없고, 응답에 노출하지 않을 값을 투영하면 쓰이지 않는 필드가 남는다.
 */
public record ShopChangeHistoryResult(
    Long id,
    ShopChangeCategory category,
    ShopChangeType changeType,
    ShopChangeActionType actionType,
    String previousValue,
    String newValue,
    LocalDateTime changedAt
) {
}
