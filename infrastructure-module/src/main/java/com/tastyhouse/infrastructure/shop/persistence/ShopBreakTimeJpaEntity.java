package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.domain.shop.domain.model.DayType;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 상점 브레이크타임 JPA 영속 모델. 순수 도메인 모델 {@code ShopBreakTime}과 분리된 영속 전용 엔티티다.
 */
@Getter
@Entity
@Table(name = "SHOP_BREAK_TIME")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopBreakTimeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Convert(converter = ShopIdConverter.class)
    @Column(name = "shop_id", nullable = false)
    private ShopId shopId; // 가게 ID (SHOP.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DayType dayType; // 요일 유형 (WEEKDAY, SATURDAY, SUNDAY, HOLIDAY 등)

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime; // 브레이크타임 시작 시각

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime; // 브레이크타임 종료 시각

    private ShopBreakTimeJpaEntity(ShopId shopId, DayType dayType, LocalTime startTime, LocalTime endTime) {
        this.shopId = shopId;
        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    static ShopBreakTimeJpaEntity create(ShopId shopId, DayType dayType, LocalTime startTime, LocalTime endTime) {
        return new ShopBreakTimeJpaEntity(shopId, dayType, startTime, endTime);
    }

    void applyChanges(DayType dayType, LocalTime startTime, LocalTime endTime) {
        this.dayType = dayType;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
