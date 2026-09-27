package com.tastyhouse.infrastructure.review.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.review.port.out.write.ReviewImageState;
import com.tastyhouse.application.review.port.out.write.ReviewImageStatePort;

import static com.tastyhouse.infrastructure.review.persistence.QReviewImageJpaEntity.reviewImageJpaEntity;

@Repository
public class ReviewImageStatePortImpl implements ReviewImageStatePort {
    private final JPAQueryFactory queryFactory;
    private final ReviewImageJpaRepository reviewImageJpaRepository;

    public ReviewImageStatePortImpl(JPAQueryFactory queryFactory, ReviewImageJpaRepository reviewImageJpaRepository) {
        this.queryFactory = queryFactory;
        this.reviewImageJpaRepository = reviewImageJpaRepository;
    }

    @Override
    public void saveAll(List<ReviewImageState> states) {
        List<ReviewImageJpaEntity> entities = states.stream()
            .map(ReviewImageMapper::toEntity)
            .toList();
        reviewImageJpaRepository.saveAll(entities);
    }

    @Override
    public void deleteByReviewId(Long reviewId) {
        queryFactory
            .delete(reviewImageJpaEntity)
            .where(reviewImageJpaEntity.reviewId.eq(reviewId))
            .execute();
    }
}
