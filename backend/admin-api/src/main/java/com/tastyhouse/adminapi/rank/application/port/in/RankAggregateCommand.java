package com.tastyhouse.adminapi.rank.application.port.in;

import java.time.LocalDate;

/**
 * 랭킹 수동 집계 command.
 *
 * <p>세 필드 모두 선택값이다 — {@code type}이 null이면 전체 타입 재집계, {@code baseDate}가 null이면
 * 오늘 기준, {@code limit}은 Request의 compact 생성자가 기본값 10을 채운다. 구조적 필수값이 없으므로
 * 별도 가드를 두지 않는다.
 */
public record RankAggregateCommand(
    String type,
    LocalDate baseDate,
    Integer limit
) {
}
