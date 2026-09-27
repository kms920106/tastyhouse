package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductShopLinkState;
import com.tastyhouse.application.product.port.out.write.ProductShopLinkStatePort;

@Repository
public class ProductShopLinkStatePortImpl implements ProductShopLinkStatePort {
    private final ProductShopLinkJpaRepository productShopLinkJpaRepository;

    public ProductShopLinkStatePortImpl(ProductShopLinkJpaRepository productShopLinkJpaRepository) {
        this.productShopLinkJpaRepository = productShopLinkJpaRepository;
    }

    @Override
    public ProductShopLinkState save(ProductShopLinkState state) {
        if (state.id() == null) {
            ProductShopLinkJpaEntity saved = productShopLinkJpaRepository
                .save(ProductShopLinkMapper.toEntity(state));
            return ProductShopLinkMapper.toState(saved);
        }

        ProductShopLinkJpaEntity entity = productShopLinkJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException(
                "존재하지 않는 메뉴-가게 연결입니다: " + state.id()));
        ProductShopLinkMapper.applyChanges(entity, state);
        return ProductShopLinkMapper.toState(entity);
    }

    @Override
    public Optional<ProductShopLinkState> findByProductIdAndShopId(Long productId, Long shopId) {
        return productShopLinkJpaRepository.findByProductIdAndShopId(productId, shopId)
            .map(ProductShopLinkMapper::toState);
    }

    @Override
    public List<ProductShopLinkState> findAllByProductId(Long productId) {
        return productShopLinkJpaRepository.findAllByProductId(productId).stream()
            .map(ProductShopLinkMapper::toState)
            .toList();
    }

    @Override
    public List<ProductShopLinkState> findAllByShopId(Long shopId) {
        return productShopLinkJpaRepository.findAllByShopIdOrderBySortAsc(shopId).stream()
            .map(ProductShopLinkMapper::toState)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndShopId(Long productId, Long shopId) {
        return productShopLinkJpaRepository.existsByProductIdAndShopId(productId, shopId);
    }

    @Override
    public long countByProductId(Long productId) {
        return productShopLinkJpaRepository.countByProductId(productId);
    }

    @Override
    public void deleteById(Long id) {
        productShopLinkJpaRepository.deleteById(id);
    }
}
