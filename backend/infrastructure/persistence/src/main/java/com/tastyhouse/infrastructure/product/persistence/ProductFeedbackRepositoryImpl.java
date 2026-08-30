package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.ProductFeedback;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.repository.ProductFeedbackRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 고객 의견 write 어댑터. 제보는 수정되지 않으므로 {@code save}는 항상 insert 경로다.
 * 목록·집계 조회는 {@code ProductFeedbackQueryDao}가 담당한다.
 */
@Repository
public class ProductFeedbackRepositoryImpl implements ProductFeedbackRepository {

    private final ProductFeedbackJpaRepository productFeedbackJpaRepository;

    public ProductFeedbackRepositoryImpl(ProductFeedbackJpaRepository productFeedbackJpaRepository) {
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
            memberId.value(), productId.value(), feedbackType, since
        );
    }

    @Override
    public boolean existsByShopIdAndCreatedAtAfter(ShopId shopId, LocalDateTime since) {
        return productFeedbackJpaRepository.existsByShopIdAndCreatedAtAfter(shopId.value(), since);
    }
}
