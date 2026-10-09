package com.tastyhouse.infrastructure.jpa.review.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.model.ReviewLike;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.out.write.ReviewLikeLoadPort;
import com.tastyhouse.application.review.port.out.write.ReviewLikeSavePort;

import static com.tastyhouse.infrastructure.jpa.review.persistence.QReviewLikeJpaEntity.reviewLikeJpaEntity;

@Repository
class ReviewLikePersistenceAdapter implements ReviewLikeLoadPort, ReviewLikeSavePort {

    private final JPAQueryFactory queryFactory;
    private final ReviewLikeJpaRepository reviewLikeJpaRepository;

    public ReviewLikePersistenceAdapter(JPAQueryFactory queryFactory, ReviewLikeJpaRepository reviewLikeJpaRepository) {
        this.queryFactory = queryFactory;
        this.reviewLikeJpaRepository = reviewLikeJpaRepository;
    }

    @Override
    public boolean existsByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId) {
        return queryFactory
            .selectOne()
            .from(reviewLikeJpaEntity)
            .where(
                reviewLikeJpaEntity.reviewId.eq(reviewId.value()),
                reviewLikeJpaEntity.memberId.eq(memberId.value())
            )
            .fetchFirst() != null;
    }

    @Override
    public void deleteByReviewIdAndMemberId(ReviewId reviewId, MemberId memberId) {
        queryFactory
            .delete(reviewLikeJpaEntity)
            .where(
                reviewLikeJpaEntity.reviewId.eq(reviewId.value()),
                reviewLikeJpaEntity.memberId.eq(memberId.value())
            )
            .execute();
    }

    @Override
    public ReviewLike save(ReviewLike reviewLike) {
        ReviewLikeJpaEntity saved = reviewLikeJpaRepository.save(ReviewLikeMapper.toEntity(reviewLike));
        return ReviewLikeMapper.toDomain(saved);
    }
}
