package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

/**
 * 상품 공통 옵션 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ProductCommonOptionJpaEntity} + {@code ProductCommonOptionMapper}가 담당한다.
 */
public class ProductCommonOption {

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

    private ProductCommonOption(
        Long id,
        ProductOptionGroupId optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible
    ) {
        this.id = id;
        this.optionGroupId = optionGroupId;
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.soldOut = soldOut;
        this.soldOutUntil = soldOutUntil;
        this.visible = visible;
    }

    public static ProductCommonOption of(
        ProductOptionGroupId optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible
    ) {
        return new ProductCommonOption(
            null,
            optionGroupId,
            name,
            additionalPrice != null ? additionalPrice : 0,
            sort,
            soldOut,
            soldOutUntil,
            visible
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ProductCommonOption reconstitute(
        Long id,
        ProductOptionGroupId optionGroupId,
        String name,
        Integer additionalPrice,
        Integer sort,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible
    ) {
        return new ProductCommonOption(id, optionGroupId, name, additionalPrice, sort, soldOut, soldOutUntil, visible);
    }

    public void update(String name, Integer additionalPrice, Integer sort, boolean soldOut, boolean visible) {
        this.name = name;
        this.additionalPrice = additionalPrice;
        this.sort = sort;
        this.soldOut = soldOut;
        this.visible = visible;

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
}
