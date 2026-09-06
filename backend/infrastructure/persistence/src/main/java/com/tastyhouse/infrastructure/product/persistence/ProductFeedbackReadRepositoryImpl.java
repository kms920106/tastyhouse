package com.tastyhouse.infrastructure.product.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductFeedbackRead;
import com.tastyhouse.domain.product.repository.ProductFeedbackReadRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public class ProductFeedbackReadRepositoryImpl implements ProductFeedbackReadRepository {
    private final ProductFeedbackReadJpaRepository productFeedbackReadJpaRepository;

    public ProductFeedbackReadRepositoryImpl(
        ProductFeedbackReadJpaRepository productFeedbackReadJpaRepository
    ) {
        this.productFeedbackReadJpaRepository = productFeedbackReadJpaRepository;
    }

    @Override
    public ProductFeedbackRead save(ProductFeedbackRead feedbackRead) {
        if (feedbackRead.getId() == null) {
            ProductFeedbackReadJpaEntity saved = productFeedbackReadJpaRepository
                .save(ProductFeedbackReadMapper.toEntity(feedbackRead));
            return ProductFeedbackReadMapper.toDomain(saved);
        }

        ProductFeedbackReadJpaEntity entity = productFeedbackReadJpaRepository
            .findById(feedbackRead.getId())
            .orElseThrow(() -> new IllegalStateException(
                "존재하지 않는 고객 의견 확인 이력입니다: " + feedbackRead.getId()));
        ProductFeedbackReadMapper.applyChanges(entity, feedbackRead);
        return ProductFeedbackReadMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductFeedbackRead> findByShopId(ShopId shopId) {
        return productFeedbackReadJpaRepository.findByShopId(shopId.value())
            .map(ProductFeedbackReadMapper::toDomain);
    }
}
