package com.tastyhouse.infrastructure.reservation.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.querydsl.core.annotations.QueryProjection;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.model.ReservationStatus;

/**
 * 예약 목록·단건 조회 결과 — 예약 행에 가게 상호명·주소·썸네일 이미지를 join해 투영한다.
 *
 * <p>내 예약 목록·예약 완료 화면·가게별 예약 목록(점주)이 같은 필드 셋을 소비하므로 하나로 공유한다.
 * 가게 이미지는 DAO가 표시용 URL까지 변환해 담으므로, 소비 모듈은 이 값을 그대로 응답에 전달한다.
 *
 * <p>{@code id}·{@code memberId}는 엔티티 컬럼 타입을 그대로 투영한다 — {@code id}는 raw {@code Long}
 * PK이고, {@code memberId}는 {@code @Convert}로 VO에 매핑된 컬럼이라 {@code MemberId}로 투영된다.
 */
public record ReservationResult(
    Long id,
    Long shopId,
    String shopName,
    String shopImageUrl,
    String shopRoadAddress,
    String shopLotAddress,
    MemberId memberId,
    LocalDate reservationDate,
    LocalTime reservationTime,
    Integer partySize,
    ReservationStatus status,
    String request,
    LocalDateTime createdAt
) {
    @QueryProjection
    public ReservationResult {
    }
}
