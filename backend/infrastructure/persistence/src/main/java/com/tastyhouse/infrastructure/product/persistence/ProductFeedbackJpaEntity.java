package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 메뉴 정보에 대한 고객 의견 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ProductFeedback}과 분리된 영속 전용 엔티티다. 변환은
 * {@code ProductFeedbackMapper}가 수행한다.
 *
 * <p>{@code feedbackType}은 {@code @Enumerated(STRING)} + {@code columnDefinition = "VARCHAR(20)"}로
 * 매핑한다 — 이 조합이 없으면 Hibernate가 기본으로 다른 타입을 기대해 {@code ddl-auto: validate}가
 * 부팅을 거부한다.
 *
 * <p>제보는 수정되지 않는 사실 기록이므로 {@code applyChanges}를 두지 않는다 — update 경로가 없다는
 * 사실의 구조적 표현이다.
 */
@Entity
@Table(name = "PRODUCT_FEEDBACK")
public class ProductFeedbackJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** 제보 시점의 가게 — 점주 목록 조회를 PRODUCT 조인 없이 처리하기 위한 비정규화 컬럼. */
    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    /** 중복 제보 판정에만 쓰며, 점주 응답에는 절대 싣지 않는다. */
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ProductFeedbackType feedbackType;

    @Column(name = "content", length = 500)
    private String content; // nullable — ETC 유형일 때만 필수

    protected ProductFeedbackJpaEntity() {
    }

    private ProductFeedbackJpaEntity(
        Long productId,
        Long shopId,
        Long memberId,
        ProductFeedbackType feedbackType,
        String content
    ) {
        this.productId = productId;
        this.shopId = shopId;
        this.memberId = memberId;
        this.feedbackType = feedbackType;
        this.content = content;
    }

    /** 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ProductFeedbackMapper#toEntity}에서만 호출한다. */
    static ProductFeedbackJpaEntity create(
        Long productId,
        Long shopId,
        Long memberId,
        ProductFeedbackType feedbackType,
        String content
    ) {
        return new ProductFeedbackJpaEntity(productId, shopId, memberId, feedbackType, content);
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public ProductFeedbackType getFeedbackType() {
        return this.feedbackType;
    }

    public String getContent() {
        return this.content;
    }
}
