package com.tastyhouse.domain.review.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 리뷰 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ReviewJpaEntity} + {@code ReviewMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code ReviewRepository#save}를
 * 호출해야 한다.
 */
public class Review {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private final ProductId productId;
    private final MemberId memberId;
    private String content;
    private Double totalRating;
    private Double tasteRating;
    private Double amountRating;
    private Double priceRating;
    private Double atmosphereRating;
    private Double kindnessRating;
    private Double hygieneRating;
    private boolean willRevisit;
    private final OrderId orderId;
    private boolean hidden;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private Review(
        Long id,
        ShopId shopId,
        ProductId productId,
        MemberId memberId,
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit,
        OrderId orderId,
        boolean hidden,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.productId = productId;
        this.memberId = memberId;
        this.content = content;
        this.totalRating = totalRating;
        this.tasteRating = tasteRating;
        this.amountRating = amountRating;
        this.priceRating = priceRating;
        this.atmosphereRating = atmosphereRating;
        this.kindnessRating = kindnessRating;
        this.hygieneRating = hygieneRating;
        this.willRevisit = willRevisit;
        this.orderId = orderId;
        this.hidden = hidden;
        this.createdAt = createdAt;
    }

    /**
     * 신규 리뷰를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static Review of(
        ShopId shopId,
        ProductId productId,
        MemberId memberId,
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit,
        OrderId orderId
    ) {
        return new Review(
            null,
            shopId,
            productId,
            memberId,
            content,
            totalRating,
            tasteRating,
            amountRating,
            priceRating,
            atmosphereRating,
            kindnessRating,
            hygieneRating,
            willRevisit,
            orderId,
            false,
            null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static Review reconstitute(
        Long id,
        ShopId shopId,
        ProductId productId,
        MemberId memberId,
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit,
        OrderId orderId,
        boolean hidden,
        LocalDateTime createdAt
    ) {
        return new Review(
            id,
            shopId,
            productId,
            memberId,
            content,
            totalRating,
            tasteRating,
            amountRating,
            priceRating,
            atmosphereRating,
            kindnessRating,
            hygieneRating,
            willRevisit,
            orderId,
            hidden,
            createdAt
        );
    }

    public ReviewId getReviewId() {
        return ReviewId.of(this.id);
    }

    public void hide() {
        this.hidden = true;
    }

    public void unhide() {
        this.hidden = false;
    }

    public void updateContent(
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit
    ) {
        this.content = content;
        this.totalRating = totalRating;
        this.tasteRating = tasteRating;
        this.amountRating = amountRating;
        this.priceRating = priceRating;
        this.atmosphereRating = atmosphereRating;
        this.kindnessRating = kindnessRating;
        this.hygieneRating = hygieneRating;
        this.willRevisit = willRevisit;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public String getContent() {
        return this.content;
    }

    public Double getTotalRating() {
        return this.totalRating;
    }

    public Double getTasteRating() {
        return this.tasteRating;
    }

    public Double getAmountRating() {
        return this.amountRating;
    }

    public Double getPriceRating() {
        return this.priceRating;
    }

    public Double getAtmosphereRating() {
        return this.atmosphereRating;
    }

    public Double getKindnessRating() {
        return this.kindnessRating;
    }

    public Double getHygieneRating() {
        return this.hygieneRating;
    }

    public boolean isWillRevisit() {
        return this.willRevisit;
    }

    public OrderId getOrderId() {
        return this.orderId;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
