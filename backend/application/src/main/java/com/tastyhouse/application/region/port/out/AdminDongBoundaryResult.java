package com.tastyhouse.application.region.port.out;

import java.math.BigDecimal;

/**
 * 행정동 경계 한 건(지도 렌더링용).
 *
 * <p>{@code boundary}는 인코딩된 원본 문자열이다 — 좌표 객체 배열로의 변환은 API 경계에서 수행하므로,
 * 이 Result는 저장 형태를 그대로 전달한다.
 *
 * <p><b>경계 미보유는 정상 상태다.</b> 시드가 단계적으로 투입되므로(코드·좌표 먼저, 경계는 나중) 좌표만
 * 있고 경계가 없는 동이 존재하며, 그 경우 {@code boundary}가 {@code null}이다. 404가 아니라 200에 담아
 * 내려보내 화면이 "경계는 못 그리지만 목록에는 있다"를 표현할 수 있게 한다.
 */
public record AdminDongBoundaryResult(
    long adminDongId,
    String regionName,
    BigDecimal centerLatitude,
    BigDecimal centerLongitude,
    String boundary
) {
}
