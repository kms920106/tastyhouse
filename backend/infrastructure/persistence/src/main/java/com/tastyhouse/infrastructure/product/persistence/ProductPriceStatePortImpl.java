package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductPriceState;
import com.tastyhouse.application.product.port.out.write.ProductPriceStatePort;

@Repository
public class ProductPriceStatePortImpl implements ProductPriceStatePort {
    private final ProductPriceJpaRepository productPriceJpaRepository;

    public ProductPriceStatePortImpl(ProductPriceJpaRepository productPriceJpaRepository) {
        this.productPriceJpaRepository = productPriceJpaRepository;
    }

    @Override
    public ProductPriceState save(ProductPriceState state) {
        if (state.id() == null) {
            ProductPriceJpaEntity saved = productPriceJpaRepository
                .save(ProductPriceMapper.toEntity(state));
            return ProductPriceMapper.toState(saved);
        }

        ProductPriceJpaEntity entity = productPriceJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException(
                "존재하지 않는 메뉴 가격입니다: " + state.id()));
        ProductPriceMapper.applyChanges(entity, state);
        return ProductPriceMapper.toState(entity);
    }

    @Override
    public Optional<ProductPriceState> findById(Long id) {
        return productPriceJpaRepository.findById(id)
            .map(ProductPriceMapper::toState);
    }

    @Override
    public List<ProductPriceState> findAllByProductId(Long productId) {
        return productPriceJpaRepository.findAllByProductIdOrderBySortAsc(productId).stream()
            .map(ProductPriceMapper::toState)
            .toList();
    }

    @Override
    public List<ProductPriceState> findAllByShopId(Long shopId) {
        return productPriceJpaRepository.findAllByShopId(shopId).stream()
            .map(ProductPriceMapper::toState)
            .toList();
    }

    @Override
    public void deleteAllByIdIn(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        productPriceJpaRepository.deleteAllByIdInBatch(ids);
    }
}
