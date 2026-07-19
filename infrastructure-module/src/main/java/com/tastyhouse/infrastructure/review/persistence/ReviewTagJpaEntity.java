package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 리뷰 태그 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ReviewTag}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 원본 엔티티와 동일하게 감사 필드(BaseEntity)를 상속하지 않는다.
 * 도메인↔엔티티 변환은 {@code ReviewTagMapper}가 수행한다.
 */
@Getter
@Entity
@Table(name = "REVIEW_TAG")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewTagJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    private ReviewTagJpaEntity(Long reviewId, Long tagId) {
        this.reviewId = reviewId;
        this.tagId = tagId;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ReviewTagMapper#toEntity}에서만 호출한다.
     */
    static ReviewTagJpaEntity create(Long reviewId, Long tagId) {
        return new ReviewTagJpaEntity(reviewId, tagId);
    }
}
