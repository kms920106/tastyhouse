package com.tastyhouse.application.shop.port.out;

import java.math.BigDecimal;

/**
 * 표현 계층에 넘기는 좌표 한 점 — 도형 계산이 끝난 뒤의 위경도 쌍.
 *
 * <p><b>챕터 09</b>에서 신설. 도형 계산은 도메인 기하 타입({@code GeoPolygon}·{@code GeoRing}·
 * {@code GeoPoint})으로 수행하는데, api 모듈은 그 타입을 알 수 없다 —
 * {@code apiModuleShouldBeDomainModelFree}의 carve-out은 {@code domain.exception..}·
 * {@code domain.shared.page..}·도메인 enum뿐이고 <b>{@code domain.shared.geo..}는 포함되지 않는다</b>.
 * 그래서 강등된 좌표를 이 record로 나른다(region 컨텍스트의
 * {@code AdminDongBoundaryViewResult.Point}와 같은 판단이다).
 *
 * <p><b>컴포넌트 선언 순서는 알파벳순({@code latitude} → {@code longitude})이다</b> — 둘 다
 * {@code BigDecimal}이라 순서가 어긋나면 컴파일은 통과하고 값만 조용히 뒤바뀐다({@code GeoPoint}가
 * 같은 이유로 세운 규칙을 따른다).
 */
public record GeoPointView(
    BigDecimal latitude,
    BigDecimal longitude
) {
}
