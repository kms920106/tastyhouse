package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;

/**
 * 매장 가격 인증 요청의 대상 항목 — 요청 1건에 메뉴 N건이 달린다.
 *
 * <p><b>요청 시점의 매장가를 이 행이 보관하는 것이 핵심이다.</b> 승인은 나중에 이뤄지므로, 그 사이
 * 점주가 가격을 바꿔도 <b>검수자가 본 가격 그대로</b> 반영돼야 한다. 승인 시점에 현재 가격을 다시
 * 읽으면 검수하지 않은 값이 승인되어 검수 자체가 무의미해진다.
 *
 * <p>{@code applyPickupSamePrice}가 켜져 있으면 승인 시 픽업가도 매장가와 같게 설정된다
 * (요구사항의 '픽업가격 동일 설정' 체크박스).
 *
 * <p>대상 메뉴·가격 행은 <b>ID VO로만</b> 참조한다 — 항목이 가격 행의 현재 값을 들고 있으면 승인 시
 * 어느 쪽이 진실인지 모호해지므로, 반영할 값({@code storePrice})만 자기 필드로 갖고 대상은 식별자로 가리킨다.
 * 실제 가격 반영은 {@code StorePriceVerificationService}가 수행한다.
 */
public class StorePriceVerificationItem {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final StorePriceVerificationId verificationId;
    private final ProductId productId;
    private final ProductPriceId productPriceId;
    private final Integer storePrice; // 요청한 매장 가격 — 승인 시 반영될 값
    private final boolean applyPickupSamePrice;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private StorePriceVerificationItem(
        Long id,
        StorePriceVerificationId verificationId,
        ProductId productId,
        ProductPriceId productPriceId,
        Integer storePrice,
        boolean applyPickupSamePrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.verificationId = verificationId;
        this.productId = productId;
        this.productPriceId = productPriceId;
        this.storePrice = storePrice;
        this.applyPickupSamePrice = applyPickupSamePrice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 새 인증 대상 항목을 만든다. */
    public static StorePriceVerificationItem of(
        StorePriceVerificationId verificationId,
        ProductId productId,
        ProductPriceId productPriceId,
        Integer storePrice,
        boolean applyPickupSamePrice
    ) {
        return new StorePriceVerificationItem(
            null,
            verificationId,
            productId,
            productPriceId,
            storePrice,
            applyPickupSamePrice,
            null,
            null
        );
    }

    /** DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다. */
    public static StorePriceVerificationItem reconstitute(
        Long id,
        StorePriceVerificationId verificationId,
        ProductId productId,
        ProductPriceId productPriceId,
        Integer storePrice,
        boolean applyPickupSamePrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new StorePriceVerificationItem(
            id,
            verificationId,
            productId,
            productPriceId,
            storePrice,
            applyPickupSamePrice,
            createdAt,
            updatedAt
        );
    }

    public Long getId() {
        return this.id;
    }

    public StorePriceVerificationId getVerificationId() {
        return this.verificationId;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public ProductPriceId getProductPriceId() {
        return this.productPriceId;
    }

    public Integer getStorePrice() {
        return this.storePrice;
    }

    public boolean isApplyPickupSamePrice() {
        return this.applyPickupSamePrice;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
