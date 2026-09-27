package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupLinkState;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupLinkStatePort;

@Repository
public class ProductCommonOptionGroupLinkStatePortImpl implements ProductCommonOptionGroupLinkStatePort {
    private final ProductCommonOptionGroupLinkJpaRepository productCommonOptionGroupLinkJpaRepository;

    public ProductCommonOptionGroupLinkStatePortImpl(
        ProductCommonOptionGroupLinkJpaRepository productCommonOptionGroupLinkJpaRepository
    ) {
        this.productCommonOptionGroupLinkJpaRepository = productCommonOptionGroupLinkJpaRepository;
    }

    @Override
    public ProductCommonOptionGroupLinkState save(ProductCommonOptionGroupLinkState state) {
        if (state.id() == null) {
            ProductCommonOptionGroupLinkJpaEntity saved =
                productCommonOptionGroupLinkJpaRepository.save(ProductCommonOptionGroupLinkMapper.toEntity(state));
            return ProductCommonOptionGroupLinkMapper.toState(saved);
        }

        ProductCommonOptionGroupLinkJpaEntity entity = productCommonOptionGroupLinkJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 옵션그룹 연결입니다: " + state.id()));
        ProductCommonOptionGroupLinkMapper.applyChanges(entity, state);
        return ProductCommonOptionGroupLinkMapper.toState(entity);
    }

    @Override
    public Optional<ProductCommonOptionGroupLinkState> findByProductIdAndOptionGroupId(Long productId, Long optionGroupId) {
        return productCommonOptionGroupLinkJpaRepository
            .findByProductIdAndOptionGroupId(productId, optionGroupId)
            .map(ProductCommonOptionGroupLinkMapper::toState);
    }

    @Override
    public List<ProductCommonOptionGroupLinkState> findAllByProductId(Long productId) {
        return productCommonOptionGroupLinkJpaRepository.findAllByProductIdOrderBySortAsc(productId).stream()
            .map(ProductCommonOptionGroupLinkMapper::toState)
            .toList();
    }

    @Override
    public List<ProductCommonOptionGroupLinkState> findAllByOptionGroupId(Long optionGroupId) {
        return productCommonOptionGroupLinkJpaRepository.findAllByOptionGroupId(optionGroupId).stream()
            .map(ProductCommonOptionGroupLinkMapper::toState)
            .toList();
    }

    @Override
    public List<ProductCommonOptionGroupLinkState> findAllByOptionGroupIdIn(List<Long> optionGroupIds) {
        if (optionGroupIds.isEmpty()) {
            return List.of();
        }
        return productCommonOptionGroupLinkJpaRepository.findAllByOptionGroupIdIn(optionGroupIds).stream()
            .map(ProductCommonOptionGroupLinkMapper::toState)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndOptionGroupId(Long productId, Long optionGroupId) {
        return productCommonOptionGroupLinkJpaRepository.existsByProductIdAndOptionGroupId(productId, optionGroupId);
    }

    @Override
    public void deleteById(Long id) {
        productCommonOptionGroupLinkJpaRepository.deleteById(id);
    }
}
