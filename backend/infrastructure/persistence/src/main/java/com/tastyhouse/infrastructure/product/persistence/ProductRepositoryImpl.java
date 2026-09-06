package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;

    public ProductRepositoryImpl(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return productJpaRepository.findByIdAndDeletedFalse(id.value()).map(ProductMapper::toDomain);
    }

    @Override
    public Optional<Product> findByIdIncludingDeleted(ProductId id) {
        return productJpaRepository.findById(id.value()).map(ProductMapper::toDomain);
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            ProductJpaEntity saved = productJpaRepository.save(ProductMapper.toEntity(product));
            return ProductMapper.toDomain(saved);
        }

        ProductJpaEntity entity = productJpaRepository.findById(product.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품입니다: " + product.getId()));
        ProductMapper.applyChanges(entity, product);
        return ProductMapper.toDomain(entity);
    }

    @Override
    public List<Product> findAllByShopIdAndIdIn(ShopId shopId, List<ProductId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        List<Long> rawIds = ids.stream().map(ProductId::value).toList();
        return productJpaRepository.findAllByShopIdAndIdInAndDeletedFalse(shopId.value(), rawIds).stream()
            .map(ProductMapper::toDomain)
            .toList();
    }

    @Override
    public long countVisibleByShopId(ShopId shopId) {
        return productJpaRepository.countVisibleByShopLink(shopId.value());
    }

    @Override
    public long countVisibleRepresentativeByShopId(ShopId shopId) {
        return productJpaRepository
            .countByShopIdAndVisibleTrueAndRepresentativeTrueAndDeletedFalse(shopId.value());
    }

    @Override
    public long countRepresentativeByShopId(ShopId shopId) {
        return productJpaRepository.countByShopIdAndRepresentativeTrueAndDeletedFalse(shopId.value());
    }

    @Override
    public List<Product> findAllSoldOutExpiredBefore(LocalDateTime baseTime) {
        return productJpaRepository
            .findAllBySoldOutTrueAndSoldOutUntilIsNotNullAndSoldOutUntilLessThanEqualAndDeletedFalse(baseTime)
            .stream()
            .map(ProductMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByShopIdAndName(ShopId shopId, String name) {
        return productJpaRepository.existsByShopIdAndNameAndDeletedFalse(shopId.value(), name);
    }

    @Override
    public boolean existsByShopIdAndNameAndIdNot(ShopId shopId, String name, ProductId excludedId) {
        return productJpaRepository
            .existsByShopIdAndNameAndIdNotAndDeletedFalse(shopId.value(), name, excludedId.value());
    }

    @Override
    public List<Product> findAllByShopIdAndCategoryId(ShopId shopId, ProductCategoryId productCategoryId) {
        List<ProductJpaEntity> entities = productCategoryId == null
            ? productJpaRepository
                .findAllByShopIdAndProductCategoryIdIsNullAndDeletedFalseOrderBySortAsc(shopId.value())
            : productJpaRepository.findAllByShopIdAndProductCategoryIdAndDeletedFalseOrderBySortAsc(
                shopId.value(), productCategoryId.value());
        return entities.stream().map(ProductMapper::toDomain).toList();
    }

    @Override
    public long countByCategoryId(ProductCategoryId productCategoryId) {
        return productJpaRepository.countByProductCategoryIdAndDeletedFalse(productCategoryId.value());
    }
}
