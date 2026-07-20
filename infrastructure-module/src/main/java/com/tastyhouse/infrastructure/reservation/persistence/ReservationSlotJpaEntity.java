package com.tastyhouse.infrastructure.reservation.persistence;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 가게 예약 슬롯 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ReservationSlot}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)과
 * 낙관적 락({@code @Version})만 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은
 * {@code ReservationSlotMapper}가 수행한다.
 */
@Getter
@Entity
@Table(
    name = "RESERVATION_SLOT",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_reservation_slot",
        columnNames = {"shop_id", "slot_date", "slot_time"}
    )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationSlotJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "slot_time", nullable = false)
    private LocalTime slotTime;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "reserved_count", nullable = false)
    private Integer reservedCount;

    @Version
    @Column(name = "version")
    private Long version;

    private ReservationSlotJpaEntity(Long shopId, LocalDate slotDate, LocalTime slotTime, Integer capacity, Integer reservedCount) {
        this.shopId = shopId;
        this.slotDate = slotDate;
        this.slotTime = slotTime;
        this.capacity = capacity;
        this.reservedCount = reservedCount;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자·버전 없음). {@code ReservationSlotMapper#toEntity}에서만 호출한다.
     */
    static ReservationSlotJpaEntity create(Long shopId, LocalDate slotDate, LocalTime slotTime, Integer capacity, Integer reservedCount) {
        return new ReservationSlotJpaEntity(shopId, slotDate, slotTime, capacity, reservedCount);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·버전은 건드리지 않는다.
     * 버전은 flush 시 JPA가 자동으로 검증·증가시켜 낙관적 락을 담당한다.
     */
    void applyChanges(Integer reservedCount) {
        this.reservedCount = reservedCount;
    }
}
