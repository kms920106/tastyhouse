package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 사장님 답변 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ReviewOwnerReply}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ReviewOwnerReplyMapper}가 수행한다.
 */
@Entity
@Table(name = "REVIEW_OWNER_REPLY")
public class ReviewOwnerReplyJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "ceo_id", nullable = false)
    private Long ceoId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    protected ReviewOwnerReplyJpaEntity() {
    }

    private ReviewOwnerReplyJpaEntity(Long reviewId, Long shopId, Long ceoId, String content) {
        this.reviewId = reviewId;
        this.shopId = shopId;
        this.ceoId = ceoId;
        this.content = content;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ReviewOwnerReplyMapper#toEntity}에서만 호출한다.
     */
    static ReviewOwnerReplyJpaEntity create(Long reviewId, Long shopId, Long ceoId, String content) {
        return new ReviewOwnerReplyJpaEntity(reviewId, shopId, ceoId, content);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·불변 필드는
     * 건드리지 않는다.
     */
    void applyChanges(String content) {
        this.content = content;
    }

    public Long getId() {
        return this.id;
    }

    public Long getReviewId() {
        return this.reviewId;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getCeoId() {
        return this.ceoId;
    }

    public String getContent() {
        return this.content;
    }
}
