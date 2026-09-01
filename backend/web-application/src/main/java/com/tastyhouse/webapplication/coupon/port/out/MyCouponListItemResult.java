package com.tastyhouse.webapplication.coupon.port.out;

import java.time.LocalDateTime;

/**
 * 내 쿠폰 목록 항목 조회 결과 — 읽기 포트 투영({@code MemberCouponResult}) + 만료 파생값.
 *
 * <p><b>챕터 10</b>에서 신설. 뒤의 두 필드({@code daysRemaining}·{@code expired})는 포트가 내려주는
 * 값이 아니라 <b>조회 시각을 기준으로 계산</b>하는 파생값이라 공용 읽기 계약 패키지
 * ({@code com.tastyhouse.application.coupon.port.out})에 형제로 둘 수 없다 — 그 패키지는 포트 하나의
 * 산출물을 담는 자리다. 시계를 읽는 계산은 application에 남고(Response의 {@code from}은 필드 복사만
 * 한다), 그래야 응답 조립이 시각에 따라 값이 달라지는 비결정 연산을 품지 않는다.
 *
 * <p>{@code discountType}은 {@code DiscountType#name()}으로 강등한 String이다 — 인바운드 포트가
 * 도메인 enum을 노출하지 않게 하려면 강등이 서비스에서 끝나야 한다.
 *
 * <p>파생 규칙은 승격 전 {@code MyCouponListItemResponse.of(...)}의 계산을 그대로 옮긴 것이다 —
 * {@code daysRemaining}은 미사용·만료 전일 때만 남은 일수이고 그 밖에는 null, {@code expired}는
 * {@code expiredAt}이 있고 이미 지났을 때만 true.
 */
public record MyCouponListItemResult(
    Long id,
    Long couponId,
    String name,
    String description,
    String discountType,
    Integer discountAmount,
    Integer maxDiscountAmount,
    Integer minOrderAmount,
    LocalDateTime useStartAt,
    LocalDateTime useEndAt,
    LocalDateTime expiredAt,
    boolean used,
    LocalDateTime usedAt,
    Long daysRemaining,
    boolean expired
) {
}
