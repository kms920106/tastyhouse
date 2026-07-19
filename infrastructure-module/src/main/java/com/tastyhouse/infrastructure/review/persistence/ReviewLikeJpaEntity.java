package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.infrastructure.persistence.converter.MemberIdConverter;
import com.tastyhouse.core.shared.entity.BaseEntity;

/**
 * 리뷰 좋아요 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ReviewLike}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ReviewLikeMapper}가 수행한다.
 */
@Getter
@Entity
@Table(name = "REVIEW_LIKE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLikeJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Convert(converter = MemberIdConverter.class)
    @Column(name = "member_id", nullable = false)
    private MemberId memberId;

    private ReviewLikeJpaEntity(Long reviewId, MemberId memberId) {
        this.reviewId = reviewId;
        this.memberId = memberId;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ReviewLikeMapper#toEntity}에서만 호출한다.
     */
    static ReviewLikeJpaEntity create(Long reviewId, MemberId memberId) {
        return new ReviewLikeJpaEntity(reviewId, memberId);
    }
}
