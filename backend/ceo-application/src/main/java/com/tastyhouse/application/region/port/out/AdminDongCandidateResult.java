package com.tastyhouse.application.region.port.out;

import java.math.BigDecimal;

/**
 * 좌표 판정 후보 행정동 한 건(반경 미리보기·도형 환산 미리보기용).
 *
 * <p>대표점 좌표와 경계를 함께 담아, 조회 측이 후보를 한 번 읽고 거리·포함 판정을 모두 끝낼 수 있게 한다.
 * 좌표만으로 판정되지 않는 동(대표점 미보유)을 위해 {@code boundary}도 함께 내려보낸다.
 *
 * <p>{@code centerLatitude}/{@code centerLongitude}/{@code boundary}는 시드 투입 단계에 따라 {@code null}일
 * 수 있다 — 판정 불가 개수를 세는 근거가 된다.
 */
public record AdminDongCandidateResult(
    long adminDongId,
    String regionName,
    BigDecimal centerLatitude,
    BigDecimal centerLongitude,
    String boundary
) {
}
