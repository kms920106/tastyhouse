package com.tastyhouse.infrastructure.review.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.review.port.out.write.ReviewLikeState;
import com.tastyhouse.application.review.port.out.write.ReviewLikeStatePort;

import static com.tastyhouse.infrastructure.review.persistence.QReviewLikeJpaEntity.reviewLikeJpaEntity;

@Repository
public class ReviewLikeStatePortImpl implements ReviewLikeStatePort {
    private final JPAQueryFactory queryFactory;
    private final ReviewLikeJpaRepository reviewLikeJpaRepository;

    public ReviewLikeStatePortImpl(JPAQueryFactory queryFactory, ReviewLikeJpaRepository reviewLikeJpaRepository) {
        this.queryFactory = queryFactory;
        this.reviewLikeJpaRepository = reviewLikeJpaRepository;
    }

    @Override
    public boolean existsByReviewIdAndMemberId(Long reviewId, Long memberId) {
        return queryFactory
            .selectOne()
            .from(reviewLikeJpaEntity)
            .where(
                reviewLikeJpaEntity.reviewId.eq(reviewId),
                reviewLikeJpaEntity.memberId.eq(memberId)
            )
            .fetchFirst() != null;
    }

    @Override
    public void deleteByReviewIdAndMemberId(Long reviewId, Long memberId) {
        queryFactory
            .delete(reviewLikeJpaEntity)
            .where(
                reviewLikeJpaEntity.reviewId.eq(reviewId),
                reviewLikeJpaEntity.memberId.eq(memberId)
            )
            .execute();
    }

    @Override
    public ReviewLikeState save(ReviewLikeState state) {
        ReviewLikeJpaEntity saved = reviewLikeJpaRepository.save(ReviewLikeMapper.toEntity(state));
        return ReviewLikeMapper.toState(saved);
    }
}
