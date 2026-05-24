package com.tastyhouse.core.domain.review.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.review.domain.model.ReviewImage;
import com.tastyhouse.core.domain.review.domain.repository.ReviewImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.domain.review.domain.model.QReviewImage.reviewImage;

@Repository
@RequiredArgsConstructor
public class ReviewImageRepositoryImpl implements ReviewImageRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewImageJpaRepository reviewImageJpaRepository;

    @Override
    public List<ReviewImage> findByReviewIdOrderBySortAsc(Long reviewId) {
        return queryFactory
            .selectFrom(reviewImage)
            .where(reviewImage.reviewId.eq(reviewId))
            .orderBy(reviewImage.sort.asc())
            .fetch();
    }

    @Override
    public List<ReviewImage> findByReviewIdInOrderBySortAsc(List<Long> reviewIds) {
        return queryFactory
            .selectFrom(reviewImage)
            .where(reviewImage.reviewId.in(reviewIds))
            .orderBy(reviewImage.sort.asc())
            .fetch();
    }

    @Override
    public void saveAll(List<ReviewImage> images) {
        reviewImageJpaRepository.saveAll(images);
    }

    @Override
    public void deleteByReviewId(Long reviewId) {
        queryFactory
            .delete(reviewImage)
            .where(reviewImage.reviewId.eq(reviewId))
            .execute();
    }
}
