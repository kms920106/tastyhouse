package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.order.domain.vo.OrderId;
import com.tastyhouse.domain.product.domain.vo.ProductId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.infrastructure.member.persistence.MemberIdConverter;
import com.tastyhouse.infrastructure.order.persistence.OrderIdConverter;
import com.tastyhouse.infrastructure.product.persistence.ProductIdConverter;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;
import com.tastyhouse.infrastructure.shop.persistence.ShopIdConverter;

/**
 * 리뷰 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Review}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ReviewMapper}가 수행한다.
 */
@Entity
@Table(name = "REVIEW")
public class ReviewJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = ShopIdConverter.class)
    @Column(name = "shop_id", nullable = false)
    private ShopId shopId;

    @Convert(converter = ProductIdConverter.class)
    @Column(name = "product_id", nullable = false)
    private ProductId productId;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "member_id", nullable = false)
    private MemberId memberId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "total_rating", nullable = false)
    private Double totalRating;

    @Column(name = "taste_rating")
    private Double tasteRating;

    @Column(name = "amount_rating")
    private Double amountRating;

    @Column(name = "price_rating")
    private Double priceRating;

    @Column(name = "atmosphere_rating")
    private Double atmosphereRating;

    @Column(name = "kindness_rating")
    private Double kindnessRating;

    @Column(name = "hygiene_rating")
    private Double hygieneRating;

    @Column(name = "will_revisit", nullable = false)
    private boolean willRevisit;

    @Convert(converter = OrderIdConverter.class)
    @Column(name = "order_id")
    private OrderId orderId;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    protected ReviewJpaEntity() {
    }

    private ReviewJpaEntity(
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
        boolean hidden
    ) {
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
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ReviewMapper#toEntity}에서만 호출한다.
     */
    static ReviewJpaEntity create(
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
        boolean hidden
    ) {
        return new ReviewJpaEntity(
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
            hidden
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit,
        boolean hidden
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
        this.hidden = hidden;
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
}
