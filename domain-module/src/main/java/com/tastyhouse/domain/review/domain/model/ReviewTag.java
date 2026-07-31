package com.tastyhouse.domain.review.domain.model;

import lombok.Getter;

/**
 * 리뷰 태그 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ReviewTagJpaEntity} + {@code ReviewTagMapper}가 담당한다. 불변 애그리거트로
 * 상태전이가 없어 감사 시각을 소비하지 않는다.
 */
@Getter
public class ReviewTag {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final Long reviewId;
    private final Long tagId;

    private ReviewTag(Long id, Long reviewId, Long tagId) {
        this.id = id;
        this.reviewId = reviewId;
        this.tagId = tagId;
    }

    /**
     * 신규 리뷰 태그를 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
    public static ReviewTag of(Long reviewId, Long tagId) {
        return new ReviewTag(null, reviewId, tagId);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ReviewTag reconstitute(Long id, Long reviewId, Long tagId) {
        return new ReviewTag(id, reviewId, tagId);
    }
}
