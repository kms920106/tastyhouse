package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;

/**
 * 가게-점주 접근권한 이력 목록 항목 조회 결과.
 *
 * <p>반드시 {@code public}이어야 한다 — package-private이면 QueryDSL {@code Projections.constructor}가
 * {@code Class#getConstructors()}(public 생성자만 반환)에서 생성자를 찾지 못해 <b>컴파일은 통과하고 그 쿼리
 * 실행 시에만 500</b>이 난다. {@code QueryResultRecordVisibilityTest}가 이를 가드한다.
 *
 * <p>{@code shopName}은 DAO가 {@code SHOP}을 join해 함께 투영한다 — 소비 Service가 가게를 재조회하지
 * 않게 하기 위함이다(query DAO는 표현에 필요한 완성 형태로 투영한다).
 *
 * <p>{@code actorAdminId}는 투영하지 않는다 — 내부 식별자이며 점주에게 노출하지 않는다.
 */
public record ShopCeoAssignmentHistoryResult(
    Long id,
    Long shopId,
    String shopName,
    ShopCeoAssignmentActionType actionType,
    LocalDateTime occurredAt
) {
}
