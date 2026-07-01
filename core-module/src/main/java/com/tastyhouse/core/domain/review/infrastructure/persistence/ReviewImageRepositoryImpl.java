package com.tastyhouse.core.domain.review.infrastructure.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewImage;
import com.tastyhouse.core.domain.review.domain.repository.ReviewImageRepository;

import static com.tastyhouse.core.domain.review.domain.model.QReviewImage.reviewImage;

@Repository
@RequiredArgsConstructor
public class ReviewImageRepositoryImpl implements ReviewImageRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewImageJpaRepository reviewImageJpaRepository;

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
