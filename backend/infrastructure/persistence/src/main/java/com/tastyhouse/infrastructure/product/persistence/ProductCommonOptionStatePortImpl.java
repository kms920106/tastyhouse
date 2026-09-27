package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductCommonOptionState;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionStatePort;

@Repository
public class ProductCommonOptionStatePortImpl implements ProductCommonOptionStatePort {
    private final ProductCommonOptionJpaRepository productCommonOptionJpaRepository;

    public ProductCommonOptionStatePortImpl(ProductCommonOptionJpaRepository productCommonOptionJpaRepository) {
        this.productCommonOptionJpaRepository = productCommonOptionJpaRepository;
    }

    @Override
    public Optional<ProductCommonOptionState> findById(Long id) {
        return productCommonOptionJpaRepository.findById(id).map(ProductCommonOptionMapper::toState);
    }

    @Override
    public ProductCommonOptionState save(ProductCommonOptionState state) {
        if (state.id() == null) {
            ProductCommonOptionJpaEntity saved =
                productCommonOptionJpaRepository.save(ProductCommonOptionMapper.toEntity(state));
            return ProductCommonOptionMapper.toState(saved);
        }

        ProductCommonOptionJpaEntity jpaEntity = productCommonOptionJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 공통 옵션입니다: " + state.id()));
        ProductCommonOptionMapper.applyChanges(jpaEntity, state);
        return ProductCommonOptionMapper.toState(jpaEntity);
    }

    @Override
    public List<ProductCommonOptionState> findAllByIdIn(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return productCommonOptionJpaRepository.findAllByIdIn(ids).stream()
            .map(ProductCommonOptionMapper::toState)
            .toList();
    }

    @Override
    public List<ProductCommonOptionState> findAllByOptionGroupId(Long optionGroupId) {
        return productCommonOptionJpaRepository.findAllByOptionGroupId(optionGroupId).stream()
            .map(ProductCommonOptionMapper::toState)
            .toList();
    }

    @Override
    public List<ProductCommonOptionState> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
        return productCommonOptionJpaRepository
            .findAllBySoldOutTrueAndSoldOutUntilIsNotNullAndSoldOutUntilLessThanEqual(baseTime).stream()
            .map(ProductCommonOptionMapper::toState)
            .toList();
    }
}
