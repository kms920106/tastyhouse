package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductFeedbackState;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackStatePort;

@Repository
public class ProductFeedbackStatePortImpl implements ProductFeedbackStatePort {
    private final ProductFeedbackJpaRepository productFeedbackJpaRepository;

    public ProductFeedbackStatePortImpl(ProductFeedbackJpaRepository productFeedbackJpaRepository) {
        this.productFeedbackJpaRepository = productFeedbackJpaRepository;
    }

    @Override
    public ProductFeedbackState save(ProductFeedbackState state) {
        ProductFeedbackJpaEntity saved = productFeedbackJpaRepository
            .save(ProductFeedbackMapper.toEntity(state));
        return ProductFeedbackMapper.toState(saved);
    }

    @Override
    public boolean existsRecentDuplicate(Long memberId, Long productId, String feedbackType, LocalDateTime since) {
        return productFeedbackJpaRepository.existsByMemberIdAndProductIdAndFeedbackTypeAndCreatedAtAfter(
            memberId, productId, feedbackType, since
        );
    }

    @Override
    public boolean existsByShopIdAndCreatedAtAfter(Long shopId, LocalDateTime since) {
        return productFeedbackJpaRepository.existsByShopIdAndCreatedAtAfter(shopId, since);
    }
}
