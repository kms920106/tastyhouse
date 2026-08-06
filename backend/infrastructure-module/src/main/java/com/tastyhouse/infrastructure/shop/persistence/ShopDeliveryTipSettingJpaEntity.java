package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.DeliveryTipExtraType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 가게 배달팁 설정 헤더 JPA 영속 모델 (가게당 1건).
 *
 * <p>거리별 설정(기본배달거리·할증 단위·할증액)을 별도 테이블로 쪼개지 않고 여기 인라인한 이유는
 * 순수 도메인 모델 {@code ShopDeliveryTipSetting} Javadoc 참고 — {@code UNIQUE(shop_id)} 행 하나가
 * 거리별↔지역별 배타성의 물리적 단일 소유자가 된다.
 *
 * <p>enum 컬럼은 {@code @Enumerated(STRING)} + {@code columnDefinition = "VARCHAR(20)"} 병기가
 * 필수다 — 빠뜨리면 Hibernate 6.4 MySQLDialect가 네이티브 {@code ENUM}을 기대해
 * {@code wrong column type ... expecting [enum]}으로 부팅이 실패한다({@code BugReport} 선례).
 */
@Entity
@Table(name = "SHOP_DELIVERY_TIP_SETTING")
public class ShopDeliveryTipSettingJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Enumerated(EnumType.STRING)
    @Column(name = "extra_tip_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DeliveryTipExtraType extraTipType; // 추가 배달팁 방식 (NONE, DISTANCE, REGION)

    @Column(name = "base_distance_meters")
    private Integer baseDistanceMeters; // 기본배달거리(m). DISTANCE일 때만

    @Enumerated(EnumType.STRING)
    @Column(name = "surcharge_unit", length = 20, columnDefinition = "VARCHAR(20)")
    private DeliveryTipDistanceUnit surchargeUnit; // 할증 단위 (PER_100M, PER_500M). DISTANCE일 때만

    @Column(name = "surcharge_amount")
    private Integer surchargeAmount; // 단위당 할증액(원). DISTANCE일 때만

    protected ShopDeliveryTipSettingJpaEntity() {
    }

    private ShopDeliveryTipSettingJpaEntity(
        Long shopId,
        DeliveryTipExtraType extraTipType,
        Integer baseDistanceMeters,
        DeliveryTipDistanceUnit surchargeUnit,
        Integer surchargeAmount
    ) {
        this.shopId = shopId;
        this.extraTipType = extraTipType;
        this.baseDistanceMeters = baseDistanceMeters;
        this.surchargeUnit = surchargeUnit;
        this.surchargeAmount = surchargeAmount;
    }

    static ShopDeliveryTipSettingJpaEntity create(
        Long shopId,
        DeliveryTipExtraType extraTipType,
        Integer baseDistanceMeters,
        DeliveryTipDistanceUnit surchargeUnit,
        Integer surchargeAmount
    ) {
        return new ShopDeliveryTipSettingJpaEntity(
            shopId, extraTipType, baseDistanceMeters, surchargeUnit, surchargeAmount
        );
    }

    void applyChanges(
        DeliveryTipExtraType extraTipType,
        Integer baseDistanceMeters,
        DeliveryTipDistanceUnit surchargeUnit,
        Integer surchargeAmount
    ) {
        this.extraTipType = extraTipType;
        this.baseDistanceMeters = baseDistanceMeters;
        this.surchargeUnit = surchargeUnit;
        this.surchargeAmount = surchargeAmount;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public DeliveryTipExtraType getExtraTipType() {
        return this.extraTipType;
    }

    public Integer getBaseDistanceMeters() {
        return this.baseDistanceMeters;
    }

    public DeliveryTipDistanceUnit getSurchargeUnit() {
        return this.surchargeUnit;
    }

    public Integer getSurchargeAmount() {
        return this.surchargeAmount;
    }
}
