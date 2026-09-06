package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 배달팁 설정 헤더 순수 도메인 모델 (가게당 1건).
 *
 * <p><b>거리별↔지역별 상호 배타 불변식의 단일 소유자</b>다. 거리별 설정은 가게당 1건이라 별도 테이블로
 * 쪼개지 않고 이 헤더에 인라인했다 — 배타성이 어느 행에도 소유자 없이 서비스 코드에만 떠 있으면
 * 동시 요청에 뚫리지만, {@code UNIQUE(shop_id)} 행 하나가 {@code extraTipType}을 들고 있으면 그 행이
 * 불변식의 물리적 단일 소유자가 된다.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopDeliveryTipSettingJpaEntity} + {@code ShopDeliveryTipSettingMapper}가 담당하며,
 * 더티 체킹이 없으므로 변경 후 명시적으로 {@code saveSetting}을 호출해야 한다.
 */
public class ShopDeliveryTipSetting {

    private final Long id;
    private final ShopId shopId;
    private DeliveryTipExtraType extraTipType;
    private Integer baseDistanceMeters;
    private DeliveryTipDistanceUnit surchargeUnit;
    private Integer surchargeAmount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopDeliveryTipSetting(
        Long id,
        ShopId shopId,
        DeliveryTipExtraType extraTipType,
        Integer baseDistanceMeters,
        DeliveryTipDistanceUnit surchargeUnit,
        Integer surchargeAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.extraTipType = extraTipType;
        this.baseDistanceMeters = baseDistanceMeters;
        this.surchargeUnit = surchargeUnit;
        this.surchargeAmount = surchargeAmount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 추가 배달팁 미사용 상태의 신규 설정 헤더를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     *
     * <p>거리별·지역별 전환은 생성 후 {@link #changeToDistance}·{@link #changeToRegion}으로 수행한다 —
     * 그래야 전환 검증 한 벌만 존재한다.
     */
    public static ShopDeliveryTipSetting of(ShopId shopId) {
        return new ShopDeliveryTipSetting(null, shopId, DeliveryTipExtraType.NONE, null, null, null, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     *
     * <p>{@link #of}·전환 메서드와 달리 검증을 하지 않는다 — 불변식 도입 이전에 저장된 기존 설정이
     * 새 규칙을 위반하더라도 로드는 가능해야 하기 때문이다.
     */
    public static ShopDeliveryTipSetting reconstitute(
        Long id,
        ShopId shopId,
        DeliveryTipExtraType extraTipType,
        Integer baseDistanceMeters,
        DeliveryTipDistanceUnit surchargeUnit,
        Integer surchargeAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopDeliveryTipSetting(
            id, shopId, extraTipType, baseDistanceMeters, surchargeUnit, surchargeAmount, createdAt, updatedAt
        );
    }

    /**
     * 거리별 추가 배달팁으로 전환한다.
     *
     * <p>기본배달거리는 {@link DeliveryTipPolicy#BASE_DISTANCE_OPTIONS} 중 하나여야 하고, 단위당 할증
     * 금액은 단위가 스스로 정한 범위({@link DeliveryTipDistanceUnit#validateAmount})를 지켜야 한다.
     *
     * <p>지역별 설정이 이미 있는지는 이 애그리거트가 알 수 없으므로(다른 애그리거트 컬렉션을 읽어야 한다)
     * {@code ShopDeliveryTipService}가 호출 전에 검증한다 — 값 자체의 불변식만 여기서 강제한다.
     */
    public void changeToDistance(int baseDistanceMeters, DeliveryTipDistanceUnit unit, int surchargeAmount) {
        if (!DeliveryTipPolicy.BASE_DISTANCE_OPTIONS.contains(baseDistanceMeters)) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_DISTANCE_BASE_INVALID,
                ErrorCode.SHOP_DELIVERY_TIP_DISTANCE_BASE_INVALID.getDefaultMessage() + " 입력: " + baseDistanceMeters + "m");
        }
        if (unit == null) {
            throw new BusinessException(ErrorCode.DELIVERY_TIP_DISTANCE_UNIT_UNKNOWN);
        }
        unit.validateAmount(surchargeAmount);

        this.extraTipType = DeliveryTipExtraType.DISTANCE;
        this.baseDistanceMeters = baseDistanceMeters;
        this.surchargeUnit = unit;
        this.surchargeAmount = surchargeAmount;
    }

    /**
     * 지역별 추가 배달팁으로 전환한다 — 거리별 설정값은 함께 비워 배타성을 값 수준에서도 유지한다.
     */
    public void changeToRegion() {
        this.extraTipType = DeliveryTipExtraType.REGION;
        this.baseDistanceMeters = null;
        this.surchargeUnit = null;
        this.surchargeAmount = null;
    }

    /**
     * 추가 배달팁을 해제한다(구간별 기본 배달팁만 남는다).
     *
     * <p>지역별 팁을 전부 삭제했을 때도 이 메서드로 {@code NONE}으로 되돌아가므로,
     * "지역별을 전부 삭제하면 거리별을 설정할 수 있다"는 규격이 별도 분기 없이 자동 성립한다.
     */
    public void clearExtraTip() {
        this.extraTipType = DeliveryTipExtraType.NONE;
        this.baseDistanceMeters = null;
        this.surchargeUnit = null;
        this.surchargeAmount = null;
    }

    /**
     * 거리별 할증액을 계산한다 — 기본배달거리 초과분을 단위 거리로 올림해 단위당 금액을 곱한다.
     *
     * <pre>
     * surcharge = ceil( max(0, meters - baseDistanceMeters) / unit.unitMeters ) * surchargeAmount
     * return min(surcharge, EXTRA_TIP_UPPER_BOUND)
     * </pre>
     *
     * <p>기본배달거리 이내면 할증이 없다(기본팁만 부과). 상한은 추가 배달팁 상한
     * ({@link DeliveryTipPolicy#EXTRA_TIP_UPPER_BOUND})으로 자른다 — 아주 먼 주소가 무제한 할증을
     * 만들지 않도록 하는 안전장치다.
     *
     * <p>거리별 설정이 아니거나 설정값이 비어 있으면 0을 돌려준다.
     */
    public int calculateDistanceSurcharge(double meters) {
        if (!usesDistance() || baseDistanceMeters == null || surchargeUnit == null || surchargeAmount == null) {
            return 0;
        }

        double excessMeters = meters - baseDistanceMeters;
        if (excessMeters <= 0) {
            return 0;
        }

        int units = (int) Math.ceil(excessMeters / surchargeUnit.getUnitMeters());
        int surcharge = units * surchargeAmount;
        return Math.min(surcharge, DeliveryTipPolicy.EXTRA_TIP_UPPER_BOUND);
    }

    /** 거리별 추가 배달팁을 사용하는 설정인지. */
    public boolean usesDistance() {
        return this.extraTipType == DeliveryTipExtraType.DISTANCE;
    }

    /** 지역별 추가 배달팁을 사용하는 설정인지. */
    public boolean usesRegion() {
        return this.extraTipType == DeliveryTipExtraType.REGION;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
