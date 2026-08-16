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
    /**
     * 사장님만보기 여부. 작성자가 등록 시점에만 정하며 이후 전환이 불가능하므로 {@code final}이다.
     *
     * <p>{@link #hidden}(관리자 게시중단)과는 직교하는 별개 축이며 동시에 참일 수 있다. 전환을 허용하지
     * 않는 이유는 상품 평점 재계산이 등록·삭제 이벤트로만 이뤄져 전환 시 평점이 조용히 어긋나기 때문이다.
     */
    private final boolean ownerOnly;
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
        boolean ownerOnly,
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
        this.ownerOnly = ownerOnly;
        this.createdAt = createdAt;
    }

    /**
     * 신규 리뷰를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     *
     * <p>{@code ownerOnly}(사장님만보기)는 등록 시점에만 정해지며 이후 전환할 수 없다 — 전환 메서드를
     * 두지 않는 것이 그 정책의 구조적 보증이다.
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
        OrderId orderId,
        boolean ownerOnly
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
            ownerOnly,
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
        boolean ownerOnly,
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
            ownerOnly,
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

    public boolean isOwnerOnly() {
        return this.ownerOnly;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
