package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 기본(구간별) 배달팁 한 구간의 순수 도메인 모델.
 *
 * <p><b>전 필드 {@code final}이다</b> — 구간 컬렉션은 개별 CRUD가 아니라 replace-all로 통째 교체하므로
 * 상태 전이가 존재하지 않는다. "3개 이하 + 금액 오름차순 + 팁 내림차순"은 집합 전체를 봐야 판정되는
 * 불변식이라, 행 단위로 고치면 어떤 순서로 조작해도 중간 상태가 규칙을 위반하기 때문이다.
 *
 * <p>여기서 강제하는 것은 <b>행 하나만 보고 판정할 수 있는 값의 불변식</b>(팁 범위·순서 범위·금액 음수)
 * 뿐이며, 집합 관계 불변식(정렬·단조성·개수)은 {@code ShopDeliveryTipService}가 담당한다.
 */
public class ShopDeliveryTipTier {

    private final Long id;
    private final ShopId shopId;
    private final int tierOrder;
    private final int minOrderAmount;
    private final int tipAmount;

    private ShopDeliveryTipTier(Long id, ShopId shopId, int tierOrder, int minOrderAmount, int tipAmount) {
        this.id = id;
        this.shopId = shopId;
        this.tierOrder = tierOrder;
        this.minOrderAmount = minOrderAmount;
        this.tipAmount = tipAmount;
    }

    /**
     * 신규 구간을 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     *
     * <p>배달팁은 {@code 0 이상 5,000원 미만}(5,000원 자체 불가 — 배민 가이드 원문),
     * 구간 하한 주문금액은 0 이상, 구간 순서는 {@code 0 ~ 2}여야 한다.
     */
    public static ShopDeliveryTipTier of(ShopId shopId, int tierOrder, int minOrderAmount, int tipAmount) {
        validateTipAmount(tipAmount);
        validateMinOrderAmount(minOrderAmount);
        validateTierOrder(tierOrder);

        return new ShopDeliveryTipTier(null, shopId, tierOrder, minOrderAmount, tipAmount);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     *
     * <p>{@link #of}와 달리 검증을 하지 않는다 — 기존 데이터가 새 불변식을 위반해도 로드는 가능해야 한다.
     */
    public static ShopDeliveryTipTier reconstitute(Long id, ShopId shopId, int tierOrder, int minOrderAmount, int tipAmount) {
        return new ShopDeliveryTipTier(id, shopId, tierOrder, minOrderAmount, tipAmount);
    }

    /**
     * 이 구간이 주어진 주문금액에 적용 가능한지 판정한다 — 주문금액이 구간 하한 이상이면 적용 가능하다.
     *
     * <p>판정 기준 주문금액은 <b>상품 할인 후 금액</b>(쿠폰·포인트 차감 전)이며, 그 값을 만드는 책임은
     * 호출부({@code ShopDeliveryTipCalculator})에 있다.
     */
    public boolean covers(int orderAmount) {
        return orderAmount >= this.minOrderAmount;
    }

    private static void validateTipAmount(int tipAmount) {
        if (tipAmount < 0 || tipAmount >= DeliveryTipPolicy.TIER_TIP_UPPER_BOUND_EXCLUSIVE) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_AMOUNT_OUT_OF_RANGE,
                ErrorCode.SHOP_DELIVERY_TIP_AMOUNT_OUT_OF_RANGE.getDefaultMessage() + " 입력: " + tipAmount + "원");
        }
    }

    private static void validateMinOrderAmount(int minOrderAmount) {
        if (minOrderAmount < 0) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_AMOUNT_OUT_OF_RANGE,
                "구간 하한 주문금액은 0원 이상이어야 합니다. 입력: " + minOrderAmount + "원");
        }
    }

    private static void validateTierOrder(int tierOrder) {
        if (tierOrder < 0 || tierOrder >= DeliveryTipPolicy.TIER_MAX_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED,
                ErrorCode.SHOP_DELIVERY_TIP_TIER_LIMIT_EXCEEDED.getDefaultMessage() + " 입력 순서: " + tierOrder);
        }
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public int getTierOrder() {
        return this.tierOrder;
    }

    public int getMinOrderAmount() {
        return this.minOrderAmount;
    }

    public int getTipAmount() {
        return this.tipAmount;
    }
}
