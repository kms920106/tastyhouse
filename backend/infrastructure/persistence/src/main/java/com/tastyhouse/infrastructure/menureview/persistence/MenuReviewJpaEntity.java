package com.tastyhouse.infrastructure.menureview.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 메뉴 평가 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code MenuReview}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code MenuReviewMapper}가 수행한다.
 *
 * <p>크로스 애그리거트 FK는 전부 raw {@code Long}이다({@code @Convert} 미사용) — VO 매핑을 하면 QueryDSL이
 * VO path를 생성해 query DAO의 raw {@code Long} 조인·투영이 깨진다.
 *
 * <p><b>{@code review_id} 컬럼이 없는 것은 의도적이다</b> — 메뉴 평가는 매장 리뷰를 참조하지 않는다.
 */
@Entity
@Table(name = "MENU_REVIEW")
public class MenuReviewJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /** 작성 근거. {@code UNIQUE(order_product_id)}가 주문 항목당 1건을 물리적으로 보증한다. */
    @Column(name = "order_product_id", nullable = false)
    private Long orderProductId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", length = 300)
    private String comment;

    @Column(name = "hidden", nullable = false)
    private boolean hidden;

    protected MenuReviewJpaEntity() {
    }

    private MenuReviewJpaEntity(
        Long memberId,
        Long shopId,
        Long productId,
        Long orderId,
        Long orderProductId,
        Integer rating,
        String comment,
        boolean hidden
    ) {
        this.memberId = memberId;
        this.shopId = shopId;
        this.productId = productId;
        this.orderId = orderId;
        this.orderProductId = orderProductId;
        this.rating = rating;
        this.comment = comment;
        this.hidden = hidden;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code MenuReviewMapper#toEntity}에서만 호출한다.
     */
    static MenuReviewJpaEntity create(
        Long memberId,
        Long shopId,
        Long productId,
        Long orderId,
        Long orderProductId,
        Integer rating,
        String comment,
        boolean hidden
    ) {
        return new MenuReviewJpaEntity(
            memberId,
            shopId,
            productId,
            orderId,
            orderProductId,
            rating,
            comment,
            hidden
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자와
     * 작성 근거({@code orderProductId})·스냅샷 FK는 건드리지 않는다.
     */
    void applyChanges(Integer rating, String comment, boolean hidden) {
        this.rating = rating;
        this.comment = comment;
        this.hidden = hidden;
    }

    public Long getId() {
        return this.id;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Long getOrderId() {
        return this.orderId;
    }

    public Long getOrderProductId() {
        return this.orderProductId;
    }

    public Integer getRating() {
        return this.rating;
    }

    public String getComment() {
        return this.comment;
    }

    public boolean isHidden() {
        return this.hidden;
    }
}
