package com.tastyhouse.infrastructure.shop.query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 라이더 안내 목록 조회의 중간 투영 결과.
 *
 * <p>"픽업 위치가 설정되었는가"를 SQL 술어로 select 절에 투영하는 대신 원본 컬럼을 그대로 읽어와,
 * 판정을 {@link #toListItem()}에서 Java로 수행한다 — 판정 기준(도로명·위경도가 모두 채워졌는가)을
 * 도메인({@code ShopRiderGuide#hasPickupLocation})·응답 조립부와 한 곳에서 일치시키기 위함이다.
 *
 * <p><b>{@code public}이어야 하는 이유</b>: {@code Projections.constructor(...)}가 만드는 QueryDSL
 * {@code ConstructorExpression}은 대상 타입의 생성자를 {@code Class#getConstructors()}로 찾는데, 이
 * 메서드는 <b>public 생성자만</b> 반환한다. package-private record의 canonical 생성자는 package-private
 * 이므로 같은 패키지의 DAO가 호출하더라도 리플렉션 탐색에서는 보이지 않아
 * {@code ExpressionException: No constructor found}로 <b>런타임에만</b> 실패한다(컴파일은 통과).
 * 따라서 이 record는 DAO 내부 전용이더라도 반드시 {@code public}으로 선언한다 —
 * 루트 {@code CLAUDE.md}의 record 파일 분리 규칙과도 일치한다.
 */
public record ShopRiderGuidePickupPresenceResult(
    Long shopId,
    String shopName,
    String visitGuide,
    String pickupRoadAddress,
    BigDecimal pickupLatitude,
    BigDecimal pickupLongitude,
    LocalDateTime updatedAt
) {

    ShopRiderGuideListItemResult toListItem() {
        boolean hasPickupLocation = pickupRoadAddress != null && pickupLatitude != null && pickupLongitude != null;

        return new ShopRiderGuideListItemResult(
            shopId,
            shopName,
            visitGuide,
            hasPickupLocation,
            updatedAt
        );
    }
}
