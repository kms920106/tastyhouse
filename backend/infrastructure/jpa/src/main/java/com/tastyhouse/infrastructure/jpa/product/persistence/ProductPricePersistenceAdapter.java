package com.tastyhouse.infrastructure.jpa.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductPriceLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductPriceSavePort;

import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductPriceJpaEntity.productPriceJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductShopLinkJpaEntity.productShopLinkJpaEntity;

@Repository
class ProductPricePersistenceAdapter implements ProductPriceLoadPort, ProductPriceSavePort {

    private final JPAQueryFactory queryFactory;
    private final ProductPriceJpaRepository productPriceJpaRepository;

    public ProductPricePersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductPriceJpaRepository productPriceJpaRepository
    ) {
        this.queryFactory = queryFactory;
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
        return queryFactory
            .selectFrom(productPriceJpaEntity)
            .where(productPriceJpaEntity.productId.eq(productId.value()))
            .orderBy(productPriceJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ProductPriceMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductPrice> findAllOfActiveProductsByShopId(ShopId shopId) {
        return queryFactory
            .select(productPriceJpaEntity)
            .from(productPriceJpaEntity, productJpaEntity, productShopLinkJpaEntity)
            .where(
                productPriceJpaEntity.productId.eq(productJpaEntity.id),
                productShopLinkJpaEntity.productId.eq(productJpaEntity.id),
                productShopLinkJpaEntity.shopId.eq(shopId.value()),
                productJpaEntity.deleted.isFalse()
            )
            .orderBy(productShopLinkJpaEntity.sort.asc(), productPriceJpaEntity.sort.asc())
            .fetch()
            .stream()
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
