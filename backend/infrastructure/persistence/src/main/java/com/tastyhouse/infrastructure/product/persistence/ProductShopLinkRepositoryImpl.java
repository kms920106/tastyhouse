package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.repository.ProductShopLinkRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Repository
public class ProductShopLinkRepositoryImpl implements ProductShopLinkRepository {
    private final ProductShopLinkJpaRepository productShopLinkJpaRepository;

    public ProductShopLinkRepositoryImpl(ProductShopLinkJpaRepository productShopLinkJpaRepository) {
        this.productShopLinkJpaRepository = productShopLinkJpaRepository;
    }

    @Override
    public ProductShopLink save(ProductShopLink link) {
        if (link.getId() == null) {
            ProductShopLinkJpaEntity saved = productShopLinkJpaRepository
                .save(ProductShopLinkMapper.toEntity(link));
            return ProductShopLinkMapper.toDomain(saved);
        }

        ProductShopLinkJpaEntity entity = productShopLinkJpaRepository.findById(link.getId())
            .orElseThrow(() -> new IllegalStateException(
                "존재하지 않는 메뉴-가게 연결입니다: " + link.getId()));
        ProductShopLinkMapper.applyChanges(entity, link);
        return ProductShopLinkMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductShopLink> findByProductIdAndShopId(ProductId productId, ShopId shopId) {
        return productShopLinkJpaRepository.findByProductIdAndShopId(productId.value(), shopId.value())
            .map(ProductShopLinkMapper::toDomain);
    }

    @Override
    public List<ProductShopLink> findAllByProductId(ProductId productId) {
        return productShopLinkJpaRepository.findAllByProductId(productId.value()).stream()
            .map(ProductShopLinkMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductShopLink> findAllByShopId(ShopId shopId) {
        return productShopLinkJpaRepository.findAllByShopIdOrderBySortAsc(shopId.value()).stream()
            .map(ProductShopLinkMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndShopId(ProductId productId, ShopId shopId) {
        return productShopLinkJpaRepository.existsByProductIdAndShopId(productId.value(), shopId.value());
    }

    @Override
    public long countByProductId(ProductId productId) {
        return productShopLinkJpaRepository.countByProductId(productId.value());
    }

    @Override
    public void delete(ProductShopLink link) {
        if (link.getId() == null) {
            return;
        }
        productShopLinkJpaRepository.deleteById(link.getId());
    }
}
