package com.tastyhouse.core.domain.review.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.review.domain.model.ReviewProduct;
import com.tastyhouse.core.domain.review.domain.repository.ReviewProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.domain.review.domain.model.QReviewProduct.reviewProduct;

@Repository
@RequiredArgsConstructor
public class ReviewProductRepositoryImpl implements ReviewProductRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewProductJpaRepository reviewProductJpaRepository;

    @Override
    public ReviewProduct save(ReviewProduct reviewProduct) {
        return reviewProductJpaRepository.save(reviewProduct);
    }

    @Override
    public List<ReviewProduct> findByReviewId(Long reviewId) {
        return queryFactory
            .selectFrom(reviewProduct)
            .where(reviewProduct.reviewId.eq(reviewId))
            .fetch();
    }

    @Override
    public void deleteByReviewId(Long reviewId) {
        queryFactory
            .delete(reviewProduct)
            .where(reviewProduct.reviewId.eq(reviewId))
            .execute();
    }
}
