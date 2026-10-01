package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.ProductFeedback;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackPersistencePort;

@Repository
public class ProductFeedbackPersistenceAdapter implements ProductFeedbackPersistencePort {

    private final ProductFeedbackJpaRepository productFeedbackJpaRepository;

    public ProductFeedbackPersistenceAdapter(ProductFeedbackJpaRepository productFeedbackJpaRepository) {
        this.productFeedbackJpaRepository = productFeedbackJpaRepository;
    }

    @Override
    public ProductFeedback save(ProductFeedback feedback) {
        ProductFeedbackJpaEntity saved = productFeedbackJpaRepository
            .save(ProductFeedbackMapper.toEntity(feedback));
        return ProductFeedbackMapper.toDomain(saved);
    }

    @Override
    public boolean existsRecentDuplicate(
        MemberId memberId,
        ProductId productId,
        ProductFeedbackType feedbackType,
        LocalDateTime since
    ) {
        return productFeedbackJpaRepository.existsByMemberIdAndProductIdAndFeedbackTypeAndCreatedAtAfter(
            memberId.value(), productId.value(), feedbackType == null ? null : feedbackType.name(), since
        );
    }

    @Override
    public boolean existsByShopIdAndCreatedAtAfter(ShopId shopId, LocalDateTime since) {
        return productFeedbackJpaRepository.existsByShopIdAndCreatedAtAfter(shopId.value(), since);
    }
}
