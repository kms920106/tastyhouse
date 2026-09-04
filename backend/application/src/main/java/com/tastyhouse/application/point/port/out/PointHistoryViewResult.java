package com.tastyhouse.application.point.port.out;

import java.util.List;

/**
 * 내 포인트 내역 화면의 조회 결과 — 잔액 요약 + 변동 내역 목록.
 *
 * <p><b>챕터 10</b>에서 신설. 잔액({@code PointBalanceResult})과 내역 목록을 <b>두 번의 포트 호출로
 * 모아 만드는 합성 결과</b>라 공용 읽기 계약 패키지에 형제로 둘 수 없다 — 그 패키지는 포트 하나의
 * 산출물을 담는 자리이고, 여기서 합성이 일어나는 지점은 application 서비스다. 컨트롤러가 대신
 * 조립하게 하면 컨트롤러가 유스케이스를 두 번 호출해야 하므로(잔액 조회 1회 추가) 경계가 흐려진다.
 *
 * <p>잔액 조회 결과가 없는 회원은 서비스가 {@code 0}으로 채운다(기존 {@code PointResponse.of(0, 0)}
 * 기본값과 동일).
 */
public record PointHistoryViewResult(
    Integer availablePoints,
    Integer expiredThisMonth,
    List<PointHistoryItemViewResult> histories
) {
}
