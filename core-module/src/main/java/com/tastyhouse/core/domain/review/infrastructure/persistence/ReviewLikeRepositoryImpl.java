package com.tastyhouse.core.domain.review.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewLike;
import com.tastyhouse.core.domain.review.domain.repository.ReviewLikeRepository;

import static com.tastyhouse.core.domain.review.domain.model.QReviewLike.reviewLike;

@Repository
@RequiredArgsConstructor
public class ReviewLikeRepositoryImpl implements ReviewLikeRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewLikeJpaRepository reviewLikeJpaRepository;

    @Override
    public boolean existsByReviewIdAndMemberId(Long reviewId, Long memberId) {
        return queryFactory
            .selectOne()
            .from(reviewLike)
            .where(
                reviewLike.reviewId.eq(reviewId),
                reviewLike.memberId.eq(memberId)
            )
            .fetchFirst() != null;
    }

    @Override
    public void deleteByReviewIdAndMemberId(Long reviewId, Long memberId) {
        queryFactory
            .delete(reviewLike)
            .where(
                reviewLike.reviewId.eq(reviewId),
                reviewLike.memberId.eq(memberId)
            )
            .execute();
    }

    @Override
    public ReviewLike save(ReviewLike reviewLike) {
        return reviewLikeJpaRepository.save(reviewLike);
    }
}
