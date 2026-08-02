package com.tastyhouse.infrastructure.review.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewLike;
import com.tastyhouse.domain.review.repository.ReviewLikeRepository;
import com.tastyhouse.domain.review.vo.ReviewId;

import static com.tastyhouse.infrastructure.review.persistence.QReviewLikeJpaEntity.reviewLikeJpaEntity;

@Repository
public class ReviewLikeRepositoryImpl implements ReviewLikeRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewLikeJpaRepository reviewLikeJpaRepository;

    public ReviewLikeRepositoryImpl(JPAQueryFactory queryFactory, ReviewLikeJpaRepository reviewLikeJpaRepository) {
        this.queryFactory = queryFactory;
        this.reviewLikeJpaRepository = reviewLikeJpaRepository;
    }

    @Override
    public boolean existsByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId) {
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
    public void deleteByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId) {
        queryFactory
            .delete(reviewLikeJpaEntity)
            .where(
                reviewLikeJpaEntity.reviewId.eq(reviewId),
                reviewLikeJpaEntity.memberId.eq(memberId)
            )
            .execute();
    }

    @Override
    public ReviewLike save(ReviewLike reviewLike) {
        ReviewLikeJpaEntity saved = reviewLikeJpaRepository.save(ReviewLikeMapper.toEntity(reviewLike));
        return ReviewLikeMapper.toDomain(saved);
    }
}
