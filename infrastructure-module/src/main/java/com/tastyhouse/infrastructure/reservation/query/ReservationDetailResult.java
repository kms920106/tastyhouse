package com.tastyhouse.infrastructure.reservation.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.reservation.domain.model.ReservationStatus;

/**
 * 예약 단건 상세 조회 결과 — {@link ReservationResult}의 가게 정보에 예약자 회원 정보(이름·휴대폰·계정)를
 * 추가로 join해 투영한다.
 *
 * <p>목록과 필드 셋이 달라(예약자 개인정보 포함) 통합하지 않고 별도 Result로 둔다 — 목록 응답에 개인정보가
 * 과잉 노출되지 않게 하기 위함이다. 가게 이미지는 DAO가 표시용 URL까지 변환해 담으므로, 소비 모듈은 이
 * 값을 그대로 응답에 전달한다.
 */
public record ReservationDetailResult(
    Long id,
    Long shopId,
    String shopName,
    String shopImageUrl,
    String shopRoadAddress,
    String shopLotAddress,
    MemberId memberId,
    String reserverName,
    String reserverPhoneNumber,
    String reserverEmail,
    LocalDate reservationDate,
    LocalTime reservationTime,
    Integer partySize,
    ReservationStatus status,
    String request,
    LocalDateTime createdAt
) {
    @QueryProjection
    public ReservationDetailResult {
    }
}
