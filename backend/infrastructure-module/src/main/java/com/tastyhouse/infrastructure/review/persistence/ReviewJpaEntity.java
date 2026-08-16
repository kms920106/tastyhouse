package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

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

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

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

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    /**
     * 사장님만보기 여부. 등록 시에만 정해지고 전환이 불가능하므로 {@link #applyChanges}의 복사 대상이
     * <b>아니다</b> — 여기에 추가하면 "언젠가 바꿀 수 있다"는 잘못된 신호가 된다.
     */
    @Column(name = "is_owner_only", nullable = false)
    private boolean ownerOnly;

    /**
     * 배달 평점(1~5). 배달 주문에만 남기며 미평가면 null이다.
     *
     * <p><b>노출은 ceo-api 점주 리뷰 상세에만 한정된다</b> — web-api 응답에는 어떤 경로로도 담지 않는다
     * (원문 규격: 고객 앱 미노출). {@code total_rating} 계산에도 넣지 않는다.
     */
    @Column(name = "delivery_rating")
    private Integer deliveryRating;

    /** 배달 평가 내용(점주 전용, 고객 앱 미노출). 미평가면 null. */
    @Column(name = "delivery_comment", length = 500)
    private String deliveryComment;

    protected ReviewJpaEntity() {
    }

    private ReviewJpaEntity(
        Long shopId,
        Long productId,
        Long memberId,
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit,
        Long orderId,
        boolean hidden,
        boolean ownerOnly,
        Integer deliveryRating,
        String deliveryComment
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
        this.ownerOnly = ownerOnly;
        this.deliveryRating = deliveryRating;
        this.deliveryComment = deliveryComment;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ReviewMapper#toEntity}에서만 호출한다.
     */
    static ReviewJpaEntity create(
        Long shopId,
        Long productId,
        Long memberId,
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit,
        Long orderId,
        boolean hidden,
        boolean ownerOnly,
        Integer deliveryRating,
        String deliveryComment
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
            hidden,
            ownerOnly,
            deliveryRating,
            deliveryComment
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
        boolean hidden,
        Integer deliveryRating,
        String deliveryComment
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
        this.deliveryRating = deliveryRating;
        this.deliveryComment = deliveryComment;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Long getMemberId() {
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

    public Long getOrderId() {
        return this.orderId;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean isOwnerOnly() {
        return this.ownerOnly;
    }

    public Integer getDeliveryRating() {
        return this.deliveryRating;
    }

    public String getDeliveryComment() {
        return this.deliveryComment;
    }
}
