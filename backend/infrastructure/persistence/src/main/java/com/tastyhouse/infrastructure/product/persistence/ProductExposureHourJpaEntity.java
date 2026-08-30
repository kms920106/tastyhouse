package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 메뉴 노출 요일·시간대 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ProductExposureHour}와 분리된 영속 전용 엔티티다. 도메인↔엔티티 변환은
 * {@code ProductExposureHourMapper}가 수행한다.
 *
 * <p>설정은 replace-all(전체 삭제 후 재삽입)로 교체하므로 {@code applyChanges}는 두지 않는다(insert 전용).
 *
 * <p>도메인 모델에 감사 필드가 없지만 {@code created_at}·{@code updated_at}이 NOT NULL이므로
 * {@code BaseEntity}를 상속한다 — 매퍼는 이 두 값을 도메인으로 옮기지 않는다.
 */
@Entity
@Table(name = "PRODUCT_EXPOSURE_HOUR")
public class ProductExposureHourJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DayType dayType;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    protected ProductExposureHourJpaEntity() {
    }

    private ProductExposureHourJpaEntity(
        Long productId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime
    ) {
        this.productId = productId;
        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductExposureHourMapper#toEntity}에서만 호출한다.
     */
    static ProductExposureHourJpaEntity create(
        Long productId,
        DayType dayType,
        LocalTime startTime,
        LocalTime endTime
    ) {
        return new ProductExposureHourJpaEntity(productId, dayType, startTime, endTime);
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public DayType getDayType() {
        return this.dayType;
    }

    public LocalTime getStartTime() {
        return this.startTime;
    }

    public LocalTime getEndTime() {
        return this.endTime;
    }
}
