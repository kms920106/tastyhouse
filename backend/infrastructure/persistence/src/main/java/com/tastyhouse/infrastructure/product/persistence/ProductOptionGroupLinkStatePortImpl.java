package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkState;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkStatePort;

@Repository
public class ProductOptionGroupLinkStatePortImpl implements ProductOptionGroupLinkStatePort {
    private final ProductOptionGroupLinkJpaRepository productOptionGroupLinkJpaRepository;

    public ProductOptionGroupLinkStatePortImpl(
        ProductOptionGroupLinkJpaRepository productOptionGroupLinkJpaRepository
    ) {
        this.productOptionGroupLinkJpaRepository = productOptionGroupLinkJpaRepository;
    }

    @Override
    public ProductOptionGroupLinkState save(ProductOptionGroupLinkState state) {
        if (state.id() == null) {
            ProductOptionGroupLinkJpaEntity saved =
                productOptionGroupLinkJpaRepository.save(ProductOptionGroupLinkMapper.toEntity(state));
            return ProductOptionGroupLinkMapper.toState(saved);
        }

        ProductOptionGroupLinkJpaEntity entity = productOptionGroupLinkJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 옵션그룹 연결입니다: " + state.id()));
        ProductOptionGroupLinkMapper.applyChanges(entity, state);
        return ProductOptionGroupLinkMapper.toState(entity);
    }

    @Override
    public Optional<ProductOptionGroupLinkState> findByProductIdAndOptionGroupId(Long productId, Long optionGroupId) {
        return productOptionGroupLinkJpaRepository
            .findByProductIdAndOptionGroupId(productId, optionGroupId)
            .map(ProductOptionGroupLinkMapper::toState);
    }

    @Override
    public List<ProductOptionGroupLinkState> findAllByProductId(Long productId) {
        return productOptionGroupLinkJpaRepository.findAllByProductIdOrderBySortAsc(productId).stream()
            .map(ProductOptionGroupLinkMapper::toState)
            .toList();
    }

    @Override
    public List<ProductOptionGroupLinkState> findAllByOptionGroupId(Long optionGroupId) {
        return productOptionGroupLinkJpaRepository.findAllByOptionGroupId(optionGroupId).stream()
            .map(ProductOptionGroupLinkMapper::toState)
            .toList();
    }

    @Override
    public List<ProductOptionGroupLinkState> findAllByOptionGroupIdIn(List<Long> optionGroupIds) {
        if (optionGroupIds.isEmpty()) {
            return List.of();
        }
        return productOptionGroupLinkJpaRepository.findAllByOptionGroupIdIn(optionGroupIds).stream()
            .map(ProductOptionGroupLinkMapper::toState)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndOptionGroupId(Long productId, Long optionGroupId) {
        return productOptionGroupLinkJpaRepository.existsByProductIdAndOptionGroupId(productId, optionGroupId);
    }

    @Override
    public void deleteById(Long id) {
        productOptionGroupLinkJpaRepository.deleteById(id);
    }
}
