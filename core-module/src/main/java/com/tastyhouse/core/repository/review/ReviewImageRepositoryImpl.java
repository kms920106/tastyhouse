package com.tastyhouse.core.repository.review;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.review.QReviewImage;
import com.tastyhouse.core.entity.review.ReviewImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewImageRepositoryImpl implements ReviewImageRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewImageJpaRepository reviewImageJpaRepository;

    @Override
    public List<ReviewImage> findByReviewIdOrderBySortAsc(Long reviewId) {
        QReviewImage reviewImage = QReviewImage.reviewImage;
        return queryFactory
            .selectFrom(reviewImage)
            .where(reviewImage.reviewId.eq(reviewId))
            .orderBy(reviewImage.sort.asc())
            .fetch();
    }

    @Override
    public List<ReviewImage> findByReviewIdInOrderBySortAsc(List<Long> reviewIds) {
        QReviewImage reviewImage = QReviewImage.reviewImage;
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
        QReviewImage reviewImage = QReviewImage.reviewImage;
        queryFactory
            .delete(reviewImage)
            .where(reviewImage.reviewId.eq(reviewId))
            .execute();
    }
}
