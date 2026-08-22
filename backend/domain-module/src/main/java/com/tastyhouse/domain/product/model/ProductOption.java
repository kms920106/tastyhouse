package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;

/**
 * 상품 옵션 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ProductOptionJpaEntity} + {@code ProductOptionMapper}가 담당한다.
 */
public class ProductOption {

    private final Long id;
    private final ProductOptionGroupId optionGroupId;
    private String name;
    private Integer additionalPrice;
    private Integer sort;
    private boolean soldOut;
    /**
     * 품절 자동해제 시각. {@code null}이면 수동 해제까지 유지되는 무기한 품절이다.
     * {@code soldOut}이 진실원이고 이 필드는 "언제 자동으로 풀리는가"만 담는다.
     */
    private LocalDateTime soldOutUntil;
    private boolean visible;
    /**
     * 이 옵션이 제공하는 일회용컵 개수(1~10). 보증금 옵션그룹의 옵션만 값을 갖고, 일반 옵션은
     * {@code null}이다.
     *
     * <p><b>금액이 아니라 개수를 저장하는 것이 핵심이다</b> — 보증금액은 {@code cupCount × 정책 요율}로
     * 계산되므로, 요율이 바뀌어도 옵션 행을 마이그레이션할 필요가 없다(과거 주문의 금액은 주문 스냅샷이
     * 별도로 보존한다).
     */
    private Integer cupCount;
    /**
     * 개인컵 사용 할인 금액(원). 개인컵 옵션이 아니면 {@code null}이다.
     *
     * <p><b>이것은 보증금 축이 아니라 상품 할인 축이다</b> — 보증금은 비과세·정산 제외 항목이지만
     * 개인컵 할인은 정상적인 매출 차감이다. 그래서 금액 계산에서 {@code productDiscountAmount}에
     * 가산되며, 개인컵 옵션은 컵을 주지 않으므로 {@code cupCount}가 없고 보증금도 0이다.
     */
    private Integer personalCupDiscountAmount;

    private ProductOption(
        Long id,
        ProductOptionGroupId optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        this.id = id;
        this.optionGroupId = optionGroupId;
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.soldOut = soldOut;
        this.soldOutUntil = soldOutUntil;
        this.visible = visible;
        this.cupCount = cupCount;
        this.personalCupDiscountAmount = personalCupDiscountAmount;
    }

    public static ProductOption of(
        ProductOptionGroupId optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        return new ProductOption(
            null,
            optionGroupId,
            name,
            additionalPrice != null ? additionalPrice : 0,
            sort,
            soldOut,
            soldOutUntil,
            visible,
            cupCount,
            personalCupDiscountAmount
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ProductOption reconstitute(
        Long id,
        ProductOptionGroupId optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        return new ProductOption(
            id,
            optionGroupId,
            name,
            additionalPrice,
            sort,
            soldOut,
            soldOutUntil,
            visible,
            cupCount,
            personalCupDiscountAmount
        );
    }

    public ProductOptionId getProductOptionId() {
        return ProductOptionId.of(this.id);
    }

    /**
     * 옵션의 이름·금액·순서·상태와 보증금 관련 값을 변경한다.
     *
     * <p>{@code cupCount}·{@code personalCupDiscountAmount}도 함께 받는 이유는 이 메서드가 전체 필드를
     * 덮어쓰는 형태이기 때문이다 — 빼면 이름만 고쳐도 컵 개수가 조용히 {@code null}이 되어 그 옵션의
     * 보증금이 0원으로 바뀐다.
     */
    public void update(
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        boolean visible,
        Integer cupCount,
        Integer personalCupDiscountAmount
    ) {
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.soldOut = soldOut;
        this.visible = visible;
        this.cupCount = cupCount;
        this.personalCupDiscountAmount = personalCupDiscountAmount;

        // 품절이 해제되는 방향이면 자동해제 시각도 함께 비워 드리프트를 남기지 않는다(Product.update와 동일).
        if (!soldOut) {
            this.soldOutUntil = null;
        }
    }

    /**
     * 기간 없이 품절 처리한다 — 수동 해제까지 유지되는 무기한 품절이다.
     */
    public void markSoldOut() {
        this.soldOut = true;
    }

    /**
     * 자동해제 시각을 지정해 품절 처리한다. 기간 유효성은 호출하는 도메인 서비스가 판정한다.
     */
    public void markSoldOut(LocalDateTime soldOutUntil) {
        this.soldOut = true;
        this.soldOutUntil = soldOutUntil;
    }

    /**
     * 품절을 해제한다. {@code soldOut}과 {@code soldOutUntil}을 <b>함께</b> 정리해야 다음 배치 주기가
     * 같은 행을 다시 집지 않는다.
     */
    public void releaseSoldOut() {
        this.soldOut = false;
        this.soldOutUntil = null;
    }

    public void hide() {
        this.visible = false;
    }

    public void activate() {
        this.visible = true;
    }

    /**
     * 품절 자동해제 시각만 변경한다. 품절 상태가 아니면 {@code PRODUCT_NOT_SOLD_OUT}(400)으로 거부한다.
     */
    public void changeSoldOutUntil(LocalDateTime until) {
        if (!this.soldOut) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_SOLD_OUT);
        }
        this.soldOutUntil = until;
    }

    public Long getId() {
        return this.id;
    }

    public ProductOptionGroupId getOptionGroupId() {
        return this.optionGroupId;
    }

    public String getName() {
        return this.name;
    }

    public Integer getAdditionalPrice() {
        return this.additionalPrice;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isSoldOut() {
        return this.soldOut;
    }

    public LocalDateTime getSoldOutUntil() {
        return this.soldOutUntil;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public Integer getCupCount() {
        return this.cupCount;
    }

    public Integer getPersonalCupDiscountAmount() {
        return this.personalCupDiscountAmount;
    }
}
