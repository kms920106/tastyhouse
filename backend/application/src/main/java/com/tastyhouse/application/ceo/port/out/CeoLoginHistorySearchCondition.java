package com.tastyhouse.application.ceo.port.out;

import java.time.LocalDate;

import com.tastyhouse.domain.ceo.model.CeoLoginResult;

/**
 * 점주 로그인 이력 목록 조회 조건.
 *
 * <p>{@code ceoId}는 필수다 — 인가가 곧 "토큰의 점주 것만 조회한다"이므로 이 값이 빠지면 남의 접속기록이
 * 통째로 새고, 동시에 이 값이 인덱스({@code ceo_id, created_at}) 레인지 스캔의 진입점이다.
 * {@code result}는 선택(null이면 전체)이다.
 *
 * @param startDate 조회 시작일(포함)
 * @param endDate 조회 종료일(포함). DAO가 반열림 구간 {@code [startDate 00:00, endDate+1d 00:00)}으로
 *     변환한다
 */
public record CeoLoginHistorySearchCondition(
    Long ceoId,
    CeoLoginResult result,
    LocalDate startDate,
    LocalDate endDate
) {

    public static CeoLoginHistorySearchCondition of(
        Long ceoId,
        CeoLoginResult result,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return new CeoLoginHistorySearchCondition(
            ceoId,
            result,
            startDate,
            endDate
        );
    }
}
