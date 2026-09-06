package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.repository.ProductPriceRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public class ProductPriceRepositoryImpl implements ProductPriceRepository {
    private final ProductPriceJpaRepository productPriceJpaRepository;

    public ProductPriceRepositoryImpl(ProductPriceJpaRepository productPriceJpaRepository) {
        this.productPriceJpaRepository = productPriceJpaRepository;
    }

    @Override
    public ProductPrice save(ProductPrice productPrice) {
        if (productPrice.getId() == null) {
            ProductPriceJpaEntity saved = productPriceJpaRepository
                .save(ProductPriceMapper.toEntity(productPrice));
            return ProductPriceMapper.toDomain(saved);
        }

        ProductPriceJpaEntity entity = productPriceJpaRepository.findById(productPrice.getId())
            .orElseThrow(() -> new IllegalStateException(
                "존재하지 않는 메뉴 가격입니다: " + productPrice.getId()));
        ProductPriceMapper.applyChanges(entity, productPrice);
        return ProductPriceMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductPrice> findById(ProductPriceId id) {
        return productPriceJpaRepository.findById(id.value())
            .map(ProductPriceMapper::toDomain);
    }

    @Override
    public List<ProductPrice> findAllByProductId(ProductId productId) {
        return productPriceJpaRepository.findAllByProductIdOrderBySortAsc(productId.value()).stream()
            .map(ProductPriceMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductPrice> findAllByShopId(ShopId shopId) {
        return productPriceJpaRepository.findAllByShopId(shopId.value()).stream()
            .map(ProductPriceMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteAllByIdIn(List<ProductPriceId> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        productPriceJpaRepository.deleteAllByIdInBatch(ids.stream().map(ProductPriceId::value).toList());
    }
}
