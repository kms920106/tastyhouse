package com.tastyhouse.domain.reservation.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.reservation.domain.vo.ReservationId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.domain.exception.AccessDeniedException;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 예약 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ReservationJpaEntity} + {@code ReservationMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code ReservationRepository#save}를
 * 호출해야 한다.
 */
public class Reservation {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final MemberId memberId; // 예약자 회원 ID
    private final ShopId shopId; // 장소 ID
    private final LocalDate reservationDate; // 예약 날짜
    private final LocalTime reservationTime; // 예약 시간
    private final Integer partySize; // 방문 인원수
    private ReservationStatus status; // 예약 상태 (상태전이로 재대입됨)
    private final String request; // 요청사항
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private Reservation(
        Long id,
        MemberId memberId,
        ShopId shopId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Integer partySize,
        ReservationStatus status,
        String request,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.shopId = shopId;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.partySize = partySize;
        this.status = status;
        this.request = request;
        this.createdAt = createdAt;
    }

    /**
     * 신규 예약을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다. 초기 상태는 PENDING이다.
     *
     * <p>방문 인원수는 1명 이상이어야 한다 — 0명·음수 예약은 슬롯 정원을 차감하면서 실제 방문은 없는
     * 모순된 상태가 된다.
     *
     * <p>과거 일시 검증은 여기 두지 않고 {@code ReservationBookingService}에 남긴다 — "지금"을 기준으로
     * 판정하는 규칙이라 값 자체의 불변식이 아니고, 시간이 지나면 이미 저장된 예약도 위반 상태가 되어
     * 재구성·재검증이 불가능해지기 때문이다(인원수는 시간과 무관한 순수 값 불변식이라 여기가 맞다).
     *
     * <p>{@link #reconstitute}는 이 검증을 <b>거치지 않는다</b> — 기존 DB 데이터가 새 불변식을 위반해도
     * 로드는 가능해야 하기 때문이다.
     */
    public static Reservation of(
        MemberId memberId,
        ShopId shopId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Integer partySize,
        String request
    ) {
        if (partySize == null || partySize < 1) {
            throw new BusinessException(ErrorCode.RESERVATION_PARTY_SIZE_INVALID,
                ErrorCode.RESERVATION_PARTY_SIZE_INVALID.getDefaultMessage() + ": " + partySize);
        }

        return new Reservation(null, memberId, shopId, reservationDate, reservationTime, partySize,
            ReservationStatus.PENDING, request, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     *
     * <p><b>{@link #of}와 달리 인원수 검증을 하지 않는다</b> — 불변식 도입 이전에 저장된 기존 예약이
     * 새 규칙을 위반하더라도 로드는 가능해야 하기 때문이다.
     */
    public static Reservation reconstitute(
        Long id,
        MemberId memberId,
        ShopId shopId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Integer partySize,
        ReservationStatus status,
        String request,
        LocalDateTime createdAt
    ) {
        return new Reservation(id, memberId, shopId, reservationDate, reservationTime, partySize, status, request, createdAt);
    }

    public ReservationId getReservationId() {
        return ReservationId.of(this.id);
    }

    /**
     * 예약자 본인 검증. 본인이 아니면 접근 거부.
     */
    public void validateOwnership(MemberId memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new AccessDeniedException(ErrorCode.RESERVATION_ACCESS_DENIED);
        }
    }

    /**
     * 점주 승인: PENDING -> CONFIRMED
     */
    public void confirm() {
        if (this.status != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.RESERVATION_INVALID_STATUS);
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    /**
     * 점주 거절: PENDING -> REJECTED
     */
    public void reject() {
        if (this.status != ReservationStatus.PENDING) {
            throw new BusinessException(ErrorCode.RESERVATION_INVALID_STATUS);
        }
        this.status = ReservationStatus.REJECTED;
    }

    /**
     * 사용자 취소: PENDING|CONFIRMED -> CANCELED
     * 취소 불가 상태는 사유별로 구분된 예외를 던져 안내 메시지를 세분화한다.
     */
    public void cancel() {
        switch (this.status) {
            case PENDING, CONFIRMED -> this.status = ReservationStatus.CANCELED;
            case CANCELED -> throw new BusinessException(ErrorCode.RESERVATION_ALREADY_CANCELED);
            case REJECTED -> throw new BusinessException(ErrorCode.RESERVATION_ALREADY_REJECTED);
            case COMPLETED -> throw new BusinessException(ErrorCode.RESERVATION_ALREADY_COMPLETED);
        }
    }

    /**
     * 방문 완료: CONFIRMED -> COMPLETED
     */
    public void complete() {
        if (this.status != ReservationStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.RESERVATION_INVALID_STATUS);
        }
        this.status = ReservationStatus.COMPLETED;
    }

    public Long getId() {
        return this.id;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public LocalDate getReservationDate() {
        return this.reservationDate;
    }

    public LocalTime getReservationTime() {
        return this.reservationTime;
    }

    public Integer getPartySize() {
        return this.partySize;
    }

    public ReservationStatus getStatus() {
        return this.status;
    }

    public String getRequest() {
        return this.request;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
