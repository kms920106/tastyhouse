package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductOptionState;
import com.tastyhouse.application.product.port.out.write.ProductOptionStatePort;

@Repository
public class ProductOptionStatePortImpl implements ProductOptionStatePort {
    private final ProductOptionJpaRepository productOptionJpaRepository;

    public ProductOptionStatePortImpl(ProductOptionJpaRepository productOptionJpaRepository) {
        this.productOptionJpaRepository = productOptionJpaRepository;
    }

    @Override
    public Optional<ProductOptionState> findById(Long id) {
        return productOptionJpaRepository.findById(id).map(ProductOptionMapper::toState);
    }

    @Override
    public ProductOptionState save(ProductOptionState state) {
        if (state.id() == null) {
            ProductOptionJpaEntity saved = productOptionJpaRepository.save(ProductOptionMapper.toEntity(state));
            return ProductOptionMapper.toState(saved);
        }

        ProductOptionJpaEntity jpaEntity = productOptionJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 옵션입니다: " + state.id()));
        ProductOptionMapper.applyChanges(jpaEntity, state);
        return ProductOptionMapper.toState(jpaEntity);
    }

    @Override
    public List<ProductOptionState> findAllByIdIn(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return productOptionJpaRepository.findAllByIdIn(ids).stream()
            .map(ProductOptionMapper::toState)
            .toList();
    }

    @Override
    public List<ProductOptionState> findAllByOptionGroupId(Long optionGroupId) {
        return productOptionJpaRepository.findAllByOptionGroupId(optionGroupId).stream()
            .map(ProductOptionMapper::toState)
            .toList();
    }

    @Override
    public List<ProductOptionState> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
        return productOptionJpaRepository
            .findAllBySoldOutTrueAndSoldOutUntilIsNotNullAndSoldOutUntilLessThanEqual(baseTime).stream()
            .map(ProductOptionMapper::toState)
            .toList();
    }
}
