package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 가게 관리 상세 조회 결과.
 *
 * <p>회원 노출용 형제인 {@link ShopVisibleDetailResult}와 달리 폐업 가게도 조회되며, 관리 화면이
 * 표시하는 운영 플래그(폐업·컵보증금)와 생성·수정 시각을 함께 담는다. 두 화면의 필드 셋이 달라
 * 통합하지 않는다(과잉 노출 방지).
 *
 * <p>과거에는 이 조회가 write 포트({@code ShopRepository#findById})로 가게 애그리거트를 로드해 필드를
 * 꺼내는 형태였고, 그 탓에 {@code ShopQueryService}가 write 포트를 들고 있어야 해 CQRS 교차 주입 금지
 * 규칙의 예외로 남아 있었다.
 */
public record ShopManagementDetailResult(
    Long id,
    Long stationId,
    String name,
    BigDecimal latitude,
    BigDecimal longitude,
    Double rating,
    String roadAddress,
    String lotAddress,
    String phoneNumber,
    boolean permanentlyClosed,
    boolean cupDepositEnabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
