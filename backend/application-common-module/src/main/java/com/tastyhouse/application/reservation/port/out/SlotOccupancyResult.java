package com.tastyhouse.application.reservation.port.out;

import java.time.LocalTime;

/**
 * 특정 가게·날짜에 <b>행이 존재하는</b> 슬롯의 점유 현황 — 슬롯 시간과 잔여 수만 담는다.
 *
 * <p>슬롯 행은 첫 예약이 들어올 때 생성되므로, 행이 없는 시간대는 예약 0건(= 정원 전체 잔여)이다.
 * 전체 슬롯 시간 목록과 병합해 "예약 가능 여부"를 판정하는 책임은 소비 모듈의 QueryService가 진다
 * (현재 시각·내 예약 보유 여부가 함께 필요한 표현 로직이므로 DAO는 DB 사실만 반환한다).
 */
public record SlotOccupancyResult(
    LocalTime slotTime,
    int remaining
) {
}
