package com.tastyhouse.core.domain.review.domain.model;

import lombok.Getter;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

/**
 * 리뷰 좋아요 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ReviewLikeJpaEntity} + {@code ReviewLikeMapper}가 담당한다. 불변 애그리거트로
 * 상태전이가 없어 감사 시각을 소비하지 않는다.
 */
@Getter
public class ReviewLike {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final Long reviewId;
    private final MemberId memberId;

    private ReviewLike(Long id, Long reviewId, MemberId memberId) {
        this.id = id;
        this.reviewId = reviewId;
        this.memberId = memberId;
    }

    /**
     * 신규 리뷰 좋아요를 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
    public static ReviewLike of(Long reviewId, MemberId memberId) {
        return new ReviewLike(null, reviewId, memberId);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ReviewLike reconstitute(Long id, Long reviewId, MemberId memberId) {
        return new ReviewLike(id, reviewId, memberId);
    }
}
