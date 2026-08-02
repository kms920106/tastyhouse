package com.tastyhouse.domain.reservation.domain.repository;

import java.time.LocalDate;
import java.util.Optional;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.reservation.domain.model.Reservation;
import com.tastyhouse.domain.reservation.domain.vo.ReservationId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 예약 write 포트.
 *
 * <p>command 경로·도메인 서비스의 트랜잭션 안에서 쓰이는 단건 로드·중복 검증·저장만 둔다. 목록·상세 등
 * 표현 목적 조회는 infrastructure-module의 {@code ReservationQueryDao}가 담당한다.
 */
public interface ReservationRepository {

    /**
     * 상태 전이 대상 예약 단건 로드.
     */
    Optional<Reservation> findById(ReservationId id);

    /**
     * 동일 회원이 동일 가게의 동일 날짜에 재예약을 막는(PENDING/CONFIRMED/COMPLETED) 예약을 보유하고 있는지.
     * 예약 생성 불변식(회원당 1일 1예약)의 검증에 쓰이므로 write 포트에 남긴다. REJECTED/CANCELED는 제외.
     */
    boolean existsBlockingByMemberShopDate(MemberId memberId, ShopId shopId, LocalDate date);

    Reservation save(Reservation reservation);
}
