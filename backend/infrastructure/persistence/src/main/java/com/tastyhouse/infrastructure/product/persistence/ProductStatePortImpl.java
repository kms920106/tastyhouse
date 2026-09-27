package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductState;
import com.tastyhouse.application.product.port.out.write.ProductStatePort;

@Repository
public class ProductStatePortImpl implements ProductStatePort {
    private final ProductJpaRepository productJpaRepository;

    public ProductStatePortImpl(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public Optional<ProductState> findById(Long id) {
        return productJpaRepository.findByIdAndDeletedFalse(id).map(ProductMapper::toState);
    }

    @Override
    public Optional<ProductState> findByIdIncludingDeleted(Long id) {
        return productJpaRepository.findById(id).map(ProductMapper::toState);
    }

    @Override
    public ProductState save(ProductState state) {
        if (state.id() == null) {
            ProductJpaEntity saved = productJpaRepository.save(ProductMapper.toEntity(state));
            return ProductMapper.toState(saved);
        }

        ProductJpaEntity entity = productJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품입니다: " + state.id()));
        ProductMapper.applyChanges(entity, state);
        return ProductMapper.toState(entity);
    }

    @Override
    public List<ProductState> findAllByShopIdAndIdIn(Long shopId, List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return productJpaRepository.findAllByShopIdAndIdInAndDeletedFalse(shopId, ids).stream()
            .map(ProductMapper::toState)
            .toList();
    }

    @Override
    public long countVisibleByShopId(Long shopId) {
        return productJpaRepository.countVisibleByShopLink(shopId);
    }

    @Override
    public long countVisibleRepresentativeByShopId(Long shopId) {
        return productJpaRepository
            .countByShopIdAndVisibleTrueAndRepresentativeTrueAndDeletedFalse(shopId);
    }

    @Override
    public long countRepresentativeByShopId(Long shopId) {
        return productJpaRepository.countByShopIdAndRepresentativeTrueAndDeletedFalse(shopId);
    }

    @Override
    public List<ProductState> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
        return productJpaRepository
            .findAllBySoldOutTrueAndSoldOutUntilIsNotNullAndSoldOutUntilLessThanEqualAndDeletedFalse(baseTime)
            .stream()
            .map(ProductMapper::toState)
            .toList();
    }

    @Override
    public boolean existsByShopIdAndName(Long shopId, String name) {
        return productJpaRepository.existsByShopIdAndNameAndDeletedFalse(shopId, name);
    }

    @Override
    public boolean existsByShopIdAndNameAndIdNot(Long shopId, String name, Long excludedId) {
        return productJpaRepository.existsByShopIdAndNameAndIdNotAndDeletedFalse(shopId, name, excludedId);
    }

    @Override
    public List<ProductState> findAllByShopIdAndCategoryId(Long shopId, Long productCategoryId) {
        List<ProductJpaEntity> entities = productCategoryId == null
            ? productJpaRepository
                .findAllByShopIdAndProductCategoryIdIsNullAndDeletedFalseOrderBySortAsc(shopId)
            : productJpaRepository.findAllByShopIdAndProductCategoryIdAndDeletedFalseOrderBySortAsc(
                shopId, productCategoryId);
        return entities.stream().map(ProductMapper::toState).toList();
    }

    @Override
    public long countByCategoryId(Long productCategoryId) {
        return productJpaRepository.countByProductCategoryIdAndDeletedFalse(productCategoryId);
    }
}
