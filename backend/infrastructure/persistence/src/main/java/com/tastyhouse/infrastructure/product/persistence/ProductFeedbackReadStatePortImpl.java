package com.tastyhouse.infrastructure.product.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductFeedbackReadState;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackReadStatePort;

@Repository
public class ProductFeedbackReadStatePortImpl implements ProductFeedbackReadStatePort {
    private final ProductFeedbackReadJpaRepository productFeedbackReadJpaRepository;

    public ProductFeedbackReadStatePortImpl(
        ProductFeedbackReadJpaRepository productFeedbackReadJpaRepository
    ) {
        this.productFeedbackReadJpaRepository = productFeedbackReadJpaRepository;
    }

    @Override
    public ProductFeedbackReadState save(ProductFeedbackReadState state) {
        if (state.id() == null) {
            ProductFeedbackReadJpaEntity saved = productFeedbackReadJpaRepository
                .save(ProductFeedbackReadMapper.toEntity(state));
            return ProductFeedbackReadMapper.toState(saved);
        }

        ProductFeedbackReadJpaEntity entity = productFeedbackReadJpaRepository
            .findById(state.id())
            .orElseThrow(() -> new IllegalStateException(
                "존재하지 않는 고객 의견 확인 이력입니다: " + state.id()));
        ProductFeedbackReadMapper.applyChanges(entity, state);
        return ProductFeedbackReadMapper.toState(entity);
    }

    @Override
    public Optional<ProductFeedbackReadState> findByShopId(Long shopId) {
        return productFeedbackReadJpaRepository.findByShopId(shopId)
            .map(ProductFeedbackReadMapper::toState);
    }
}
