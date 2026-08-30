package com.tastyhouse.infrastructure.shop.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 라이더 가게방문 안내 JPA 영속 모델. 순수 도메인 모델 {@code ShopRiderGuide}와 분리된 영속 전용 엔티티다.
 *
 * <p>픽업 위치 5개 컬럼은 {@code @Embedded} VO로 묶지 않고 평면으로 둔다 — VO로 묶으려면 record여야 하고
 * 컴포넌트 선언 순서가 알파벳 오름차순이어야 하는 제약이 붙는데, 얻는 것이 없다.
 */
@Entity
@Table(name = "SHOP_RIDER_GUIDE")
public class ShopRiderGuideJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "visit_guide", length = 200)
    private String visitGuide; // 라이더 가게방문 안내 문구

    @Column(name = "pickup_road_address")
    private String pickupRoadAddress; // 픽업 도로명주소

    @Column(name = "pickup_lot_address")
    private String pickupLotAddress; // 픽업 지번주소

    @Column(name = "pickup_detail_address", length = 100)
    private String pickupDetailAddress; // 픽업 상세주소

    @Column(name = "pickup_latitude", precision = 11, scale = 8)
    private BigDecimal pickupLatitude; // 픽업 위도

    @Column(name = "pickup_longitude", precision = 11, scale = 8)
    private BigDecimal pickupLongitude; // 픽업 경도

    protected ShopRiderGuideJpaEntity() {
    }

    private ShopRiderGuideJpaEntity(
        Long shopId,
        String visitGuide,
        String pickupRoadAddress,
        String pickupLotAddress,
        String pickupDetailAddress,
        BigDecimal pickupLatitude,
        BigDecimal pickupLongitude
    ) {
        this.shopId = shopId;
        this.visitGuide = visitGuide;
        this.pickupRoadAddress = pickupRoadAddress;
        this.pickupLotAddress = pickupLotAddress;
        this.pickupDetailAddress = pickupDetailAddress;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopRiderGuideMapper#toEntity}에서만 호출한다.
     */
    static ShopRiderGuideJpaEntity create(
        Long shopId,
        String visitGuide,
        String pickupRoadAddress,
        String pickupLotAddress,
        String pickupDetailAddress,
        BigDecimal pickupLatitude,
        BigDecimal pickupLongitude
    ) {
        return new ShopRiderGuideJpaEntity(shopId, visitGuide, pickupRoadAddress, pickupLotAddress,
            pickupDetailAddress, pickupLatitude, pickupLongitude);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·shopId는 건드리지 않는다.
     */
    void applyChanges(
        String visitGuide,
        String pickupRoadAddress,
        String pickupLotAddress,
        String pickupDetailAddress,
        BigDecimal pickupLatitude,
        BigDecimal pickupLongitude
    ) {
        this.visitGuide = visitGuide;
        this.pickupRoadAddress = pickupRoadAddress;
        this.pickupLotAddress = pickupLotAddress;
        this.pickupDetailAddress = pickupDetailAddress;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getVisitGuide() {
        return this.visitGuide;
    }

    public String getPickupRoadAddress() {
        return this.pickupRoadAddress;
    }

    public String getPickupLotAddress() {
        return this.pickupLotAddress;
    }

    public String getPickupDetailAddress() {
        return this.pickupDetailAddress;
    }

    public BigDecimal getPickupLatitude() {
        return this.pickupLatitude;
    }

    public BigDecimal getPickupLongitude() {
        return this.pickupLongitude;
    }
}
