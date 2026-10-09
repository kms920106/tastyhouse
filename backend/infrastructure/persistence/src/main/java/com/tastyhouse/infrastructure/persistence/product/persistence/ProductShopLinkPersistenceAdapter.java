package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductShopLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductShopLinkLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductShopLinkSavePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductShopLinkJpaEntity.productShopLinkJpaEntity;

@Repository
class ProductShopLinkPersistenceAdapter implements ProductShopLinkLoadPort, ProductShopLinkSavePort {

    private final JPAQueryFactory queryFactory;
    private final ProductShopLinkJpaRepository productShopLinkJpaRepository;

    public ProductShopLinkPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductShopLinkJpaRepository productShopLinkJpaRepository
    ) {
        this.queryFactory = queryFactory;
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
        ProductShopLinkJpaEntity entity = queryFactory
            .selectFrom(productShopLinkJpaEntity)
            .where(
                productShopLinkJpaEntity.productId.eq(productId.value()),
                productShopLinkJpaEntity.shopId.eq(shopId.value())
            )
            .fetchOne();
        return Optional.ofNullable(entity).map(ProductShopLinkMapper::toDomain);
    }

    @Override
    public List<ProductShopLink> findAllByProductId(ProductId productId) {
        return queryFactory
            .selectFrom(productShopLinkJpaEntity)
            .where(productShopLinkJpaEntity.productId.eq(productId.value()))
            .fetch()
            .stream()
            .map(ProductShopLinkMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductShopLink> findAllByShopId(ShopId shopId) {
        return queryFactory
            .selectFrom(productShopLinkJpaEntity)
            .where(productShopLinkJpaEntity.shopId.eq(shopId.value()))
            .orderBy(productShopLinkJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ProductShopLinkMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndShopId(ProductId productId, ShopId shopId) {
        return queryFactory
            .selectOne()
            .from(productShopLinkJpaEntity)
            .where(
                productShopLinkJpaEntity.productId.eq(productId.value()),
                productShopLinkJpaEntity.shopId.eq(shopId.value())
            )
            .fetchFirst() != null;
    }

    @Override
    public long countByProductId(ProductId productId) {
        Long count = queryFactory
            .select(productShopLinkJpaEntity.count())
            .from(productShopLinkJpaEntity)
            .where(productShopLinkJpaEntity.productId.eq(productId.value()))
            .fetchOne();
        return count == null ? 0L : count;
    }

    @Override
    public void delete(ProductShopLink link) {
        if (link.getId() == null) {
            return;
        }
        productShopLinkJpaRepository.deleteById(link.getId());
    }
}
