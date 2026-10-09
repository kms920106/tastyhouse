package com.tastyhouse.infrastructure.jpa.product.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductSavePort;

import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductShopLinkJpaEntity.productShopLinkJpaEntity;

@Repository
class ProductPersistenceAdapter implements ProductLoadPort, ProductSavePort {

    private final JPAQueryFactory queryFactory;
    private final ProductJpaRepository productJpaRepository;

    public ProductPersistenceAdapter(JPAQueryFactory queryFactory, ProductJpaRepository productJpaRepository) {
        this.queryFactory = queryFactory;
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public Optional<Product> findActiveById(ProductId id) {
        ProductJpaEntity entity = queryFactory
            .selectFrom(productJpaEntity)
            .where(productJpaEntity.id.eq(id.value()), productJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(ProductMapper::toDomain);
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
    public List<Product> findAllActiveByShopIdAndIdIn(ShopId shopId, List<ProductId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productJpaEntity)
            .where(
                productJpaEntity.shopId.eq(shopId.value()),
                productJpaEntity.id.in(ids.stream().map(ProductId::value).toList()),
                productJpaEntity.deleted.isFalse()
            )
            .fetch()
            .stream()
            .map(ProductMapper::toDomain)
            .toList();
    }

    @Override
    public long countVisibleByShopId(ShopId shopId) {
        Long count = queryFactory
            .select(productJpaEntity.id.countDistinct())
            .from(productJpaEntity, productShopLinkJpaEntity)
            .where(
                productShopLinkJpaEntity.productId.eq(productJpaEntity.id),
                productShopLinkJpaEntity.shopId.eq(shopId.value()),
                productJpaEntity.visible.isTrue(),
                productJpaEntity.deleted.isFalse()
            )
            .fetchOne();
        return count == null ? 0L : count;
    }

    @Override
    public long countVisibleRepresentativeByShopId(ShopId shopId) {
        Long count = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .where(
                productJpaEntity.shopId.eq(shopId.value()),
                productJpaEntity.visible.isTrue(),
                productJpaEntity.representative.isTrue(),
                productJpaEntity.deleted.isFalse()
            )
            .fetchOne();
        return count == null ? 0L : count;
    }

    @Override
    public long countActiveRepresentativeByShopId(ShopId shopId) {
        Long count = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .where(
                productJpaEntity.shopId.eq(shopId.value()),
                productJpaEntity.representative.isTrue(),
                productJpaEntity.deleted.isFalse()
            )
            .fetchOne();
        return count == null ? 0L : count;
    }

    @Override
    public List<Product> findAllActiveSoldOutExpiredBefore(LocalDateTime baseTime) {
        return queryFactory
            .selectFrom(productJpaEntity)
            .where(
                productJpaEntity.soldOut.isTrue(),
                productJpaEntity.soldOutUntil.isNotNull(),
                productJpaEntity.soldOutUntil.loe(baseTime),
                productJpaEntity.deleted.isFalse()
            )
            .fetch()
            .stream()
            .map(ProductMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsActiveByShopIdAndName(ShopId shopId, String name) {
        return queryFactory
            .selectOne()
            .from(productJpaEntity)
            .where(
                productJpaEntity.shopId.eq(shopId.value()),
                productJpaEntity.name.eq(name),
                productJpaEntity.deleted.isFalse()
            )
            .fetchFirst() != null;
    }

    @Override
    public boolean existsActiveByShopIdAndNameAndIdNot(ShopId shopId, String name, ProductId excludedId) {
        return queryFactory
            .selectOne()
            .from(productJpaEntity)
            .where(
                productJpaEntity.shopId.eq(shopId.value()),
                productJpaEntity.name.eq(name),
                productJpaEntity.id.ne(excludedId.value()),
                productJpaEntity.deleted.isFalse()
            )
            .fetchFirst() != null;
    }

    @Override
    public List<Product> findAllActiveByShopIdAndCategoryId(ShopId shopId, ProductCategoryId productCategoryId) {
        Long categoryId = productCategoryId == null ? null : productCategoryId.value();
        return queryFactory
            .selectFrom(productJpaEntity)
            .where(
                productJpaEntity.shopId.eq(shopId.value()),
                productCategoryIdEq(categoryId),
                productJpaEntity.deleted.isFalse()
            )
            .orderBy(productJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ProductMapper::toDomain)
            .toList();
    }

    @Override
    public long countActiveByCategoryId(ProductCategoryId productCategoryId) {
        Long count = queryFactory
            .select(productJpaEntity.count())
            .from(productJpaEntity)
            .where(
                productJpaEntity.productCategoryId.eq(productCategoryId.value()),
                productJpaEntity.deleted.isFalse()
            )
            .fetchOne();
        return count == null ? 0L : count;
    }

    private BooleanExpression productCategoryIdEq(Long categoryId) {
        return categoryId == null
            ? productJpaEntity.productCategoryId.isNull()
            : productJpaEntity.productCategoryId.eq(categoryId);
    }
}
