package com.tastyhouse.infrastructure.shop.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 가게 편의정보 JPA 영속 모델. 순수 도메인 모델 {@code ShopConvenienceInfo}와 분리된 영속 전용 엔티티다.
 */
@Entity
@Table(name = "SHOP_CONVENIENCE_INFO")
public class ShopConvenienceInfoJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = ShopIdConverter.class)
    @Column(name = "shop_id", nullable = false)
    private ShopId shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "is_parking_available", nullable = false)
    private boolean parkingAvailable; // 주차 가능 여부

    @Column(name = "is_parking_paid", nullable = false)
    private boolean parkingPaid; // 주차 유료 여부

    @Column(name = "is_valet_available", nullable = false)
    private boolean valetAvailable; // 발렛 가능 여부

    @Column(name = "is_valet_paid", nullable = false)
    private boolean valetPaid; // 발렛 유료 여부

    @Column(name = "directions_guide", length = 200)
    private String directionsGuide; // 찾아오는 길 안내

    @Column(name = "display_latitude", precision = 9, scale = 6)
    private BigDecimal displayLatitude; // 노출 위치 위도

    @Column(name = "display_longitude", precision = 9, scale = 6)
    private BigDecimal displayLongitude; // 노출 위치 경도

    protected ShopConvenienceInfoJpaEntity() {
    }

    private ShopConvenienceInfoJpaEntity(
        ShopId shopId,
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude
    ) {
        this.shopId = shopId;
        this.parkingAvailable = parkingAvailable;
        this.parkingPaid = parkingPaid;
        this.valetAvailable = valetAvailable;
        this.valetPaid = valetPaid;
        this.directionsGuide = directionsGuide;
        this.displayLatitude = displayLatitude;
        this.displayLongitude = displayLongitude;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopConvenienceInfoMapper#toEntity}에서만 호출한다.
     */
    static ShopConvenienceInfoJpaEntity create(
        ShopId shopId,
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude
    ) {
        return new ShopConvenienceInfoJpaEntity(shopId, parkingAvailable, parkingPaid, valetAvailable, valetPaid,
            directionsGuide, displayLatitude, displayLongitude);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·shopId는 건드리지 않는다.
     */
    void applyChanges(
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude
    ) {
        this.parkingAvailable = parkingAvailable;
        this.parkingPaid = parkingPaid;
        this.valetAvailable = valetAvailable;
        this.valetPaid = valetPaid;
        this.directionsGuide = directionsGuide;
        this.displayLatitude = displayLatitude;
        this.displayLongitude = displayLongitude;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public boolean isParkingAvailable() {
        return this.parkingAvailable;
    }

    public boolean isParkingPaid() {
        return this.parkingPaid;
    }

    public boolean isValetAvailable() {
        return this.valetAvailable;
    }

    public boolean isValetPaid() {
        return this.valetPaid;
    }

    public String getDirectionsGuide() {
        return this.directionsGuide;
    }

    public BigDecimal getDisplayLatitude() {
        return this.displayLatitude;
    }

    public BigDecimal getDisplayLongitude() {
        return this.displayLongitude;
    }
}
