package com.tastyhouse.infrastructure.review.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.domain.model.ReviewTag;
import com.tastyhouse.domain.review.domain.repository.ReviewTagRepository;
import com.tastyhouse.domain.review.domain.vo.ReviewId;

import static com.tastyhouse.infrastructure.review.persistence.QReviewTagJpaEntity.reviewTagJpaEntity;

@Repository
public class ReviewTagRepositoryImpl implements ReviewTagRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewTagJpaRepository reviewTagJpaRepository;

    public ReviewTagRepositoryImpl(JPAQueryFactory queryFactory, ReviewTagJpaRepository reviewTagJpaRepository) {
        this.queryFactory = queryFactory;
        this.reviewTagJpaRepository = reviewTagJpaRepository;
    }

    @Override
    public void saveAll(List<ReviewTag> tags) {
        List<ReviewTagJpaEntity> entities = tags.stream()
            .map(ReviewTagMapper::toEntity)
            .toList();
        reviewTagJpaRepository.saveAll(entities);
    }

    @Override
    public void deleteByReviewId(ReviewId reviewId) {
        queryFactory
            .delete(reviewTagJpaEntity)
            .where(reviewTagJpaEntity.reviewId.eq(reviewId))
            .execute();
    }
}
