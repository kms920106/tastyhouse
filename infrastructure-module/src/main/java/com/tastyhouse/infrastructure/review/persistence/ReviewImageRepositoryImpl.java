package com.tastyhouse.infrastructure.review.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.domain.model.ReviewImage;
import com.tastyhouse.domain.review.domain.repository.ReviewImageRepository;
import com.tastyhouse.domain.review.domain.vo.ReviewId;

import static com.tastyhouse.infrastructure.review.persistence.QReviewImageJpaEntity.reviewImageJpaEntity;

@Repository
@RequiredArgsConstructor
public class ReviewImageRepositoryImpl implements ReviewImageRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewImageJpaRepository reviewImageJpaRepository;

    @Override
    public void saveAll(List<ReviewImage> images) {
        List<ReviewImageJpaEntity> entities = images.stream()
            .map(ReviewImageMapper::toEntity)
            .toList();
        reviewImageJpaRepository.saveAll(entities);
    }

    @Override
    public void deleteByReviewId(ReviewId reviewId) {
        queryFactory
            .delete(reviewImageJpaEntity)
            .where(reviewImageJpaEntity.reviewId.eq(reviewId))
            .execute();
    }
}
