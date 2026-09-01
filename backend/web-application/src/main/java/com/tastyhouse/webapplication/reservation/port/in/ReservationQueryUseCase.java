package com.tastyhouse.webapplication.reservation.port.in;

import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.application.reservation.port.out.ReservationResult;
import com.tastyhouse.webapplication.reservation.port.out.ReservationCompleteDetailResult;
import com.tastyhouse.webapplication.reservation.port.out.ReservationDetailViewResult;
import com.tastyhouse.webapplication.reservation.port.out.ReservationSlotAvailabilityResult;

/**
 * 예약 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ReservationQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p><b>챕터 10</b> — 반환 타입이 표현 계약에서 프레임워크-프리 읽기 계약으로 바뀌었다. 슬롯 가용성
 * 판정(시계·정원 정책)과 본인 예약 검증은 여전히 구현이 하고, 표현 계약 조립만 web-api로 올라갔다.
 */
public interface ReservationQueryUseCase {

    ReservationSlotAvailabilityResult getAvailability(Long shopId, LocalDate date, Long memberId);

    List<ReservationResult> getMyReservations(Long memberId);

    List<ReservationResult> getShopReservations(Long shopId);

    ReservationResult getReservation(Long id);

    ReservationCompleteDetailResult getCompleteDetail(Long memberId, Long id);

    ReservationDetailViewResult getReservationDetail(Long memberId, Long id);
}
