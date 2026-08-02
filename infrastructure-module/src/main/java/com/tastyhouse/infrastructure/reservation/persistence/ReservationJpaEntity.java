package com.tastyhouse.infrastructure.reservation.persistence;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.reservation.domain.model.ReservationStatus;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.infrastructure.member.persistence.MemberIdConverter;
import com.tastyhouse.infrastructure.shop.persistence.ShopIdConverter;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 예약 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Reservation}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ReservationMapper}가 수행한다.
 */
@Entity
@Table(
    name = "RESERVATION",
    indexes = {
        @Index(name = "idx_reservation_shop_slot", columnList = "shop_id, reservation_date, reservation_time"),
        @Index(name = "idx_reservation_member", columnList = "member_id")
    }
)
public class ReservationJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "member_id", nullable = false)
    private MemberId memberId;

    @Convert(converter = ShopIdConverter.class)
    @Column(name = "shop_id", nullable = false)
    private ShopId shopId;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "reservation_time", nullable = false)
    private LocalTime reservationTime;

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ReservationStatus status;

    @Column(name = "request", columnDefinition = "TEXT")
    private String request;

    protected ReservationJpaEntity() {
    }

    private ReservationJpaEntity(
        MemberId memberId,
        ShopId shopId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Integer partySize,
        ReservationStatus status,
        String request
    ) {
        this.memberId = memberId;
        this.shopId = shopId;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.partySize = partySize;
        this.status = status;
        this.request = request;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ReservationMapper#toEntity}에서만 호출한다.
     */
    static ReservationJpaEntity create(
        MemberId memberId,
        ShopId shopId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        Integer partySize,
        ReservationStatus status,
        String request
    ) {
        return new ReservationJpaEntity(memberId, shopId, reservationDate, reservationTime, partySize, status, request);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(ReservationStatus status) {
        this.status = status;
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
}
