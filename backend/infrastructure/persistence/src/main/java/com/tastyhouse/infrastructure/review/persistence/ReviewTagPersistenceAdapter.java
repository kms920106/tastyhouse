package com.tastyhouse.infrastructure.review.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ReviewTag;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.application.review.port.out.write.ReviewTagPersistencePort;

import static com.tastyhouse.infrastructure.review.persistence.QReviewTagJpaEntity.reviewTagJpaEntity;

@Repository
public class ReviewTagPersistenceAdapter implements ReviewTagPersistencePort {
    private final JPAQueryFactory queryFactory;
    private final ReviewTagJpaRepository reviewTagJpaRepository;

    public ReviewTagPersistenceAdapter(JPAQueryFactory queryFactory, ReviewTagJpaRepository reviewTagJpaRepository) {
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
            .where(reviewTagJpaEntity.reviewId.eq(reviewId.value()))
            .execute();
    }
}
