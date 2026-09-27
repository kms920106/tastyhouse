package com.tastyhouse.infrastructure.review.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.review.port.out.write.ReviewTagState;
import com.tastyhouse.application.review.port.out.write.ReviewTagStatePort;

import static com.tastyhouse.infrastructure.review.persistence.QReviewTagJpaEntity.reviewTagJpaEntity;

@Repository
public class ReviewTagStatePortImpl implements ReviewTagStatePort {
    private final JPAQueryFactory queryFactory;
    private final ReviewTagJpaRepository reviewTagJpaRepository;

    public ReviewTagStatePortImpl(JPAQueryFactory queryFactory, ReviewTagJpaRepository reviewTagJpaRepository) {
        this.queryFactory = queryFactory;
        this.reviewTagJpaRepository = reviewTagJpaRepository;
    }

    @Override
    public void saveAll(List<ReviewTagState> states) {
        List<ReviewTagJpaEntity> entities = states.stream()
            .map(ReviewTagMapper::toEntity)
            .toList();
        reviewTagJpaRepository.saveAll(entities);
    }

    @Override
    public void deleteByReviewId(Long reviewId) {
        queryFactory
            .delete(reviewTagJpaEntity)
            .where(reviewTagJpaEntity.reviewId.eq(reviewId))
            .execute();
    }
}
