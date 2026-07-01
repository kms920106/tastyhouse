package com.tastyhouse.core.domain.review.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.review.domain.model.ReviewProduct;
import com.tastyhouse.core.domain.review.domain.repository.ReviewProductRepository;

@Repository
@RequiredArgsConstructor
public class ReviewProductRepositoryImpl implements ReviewProductRepository {

    private final ReviewProductJpaRepository reviewProductJpaRepository;

    @Override
    public ReviewProduct save(ReviewProduct reviewProduct) {
        return reviewProductJpaRepository.save(reviewProduct);
    }
}
