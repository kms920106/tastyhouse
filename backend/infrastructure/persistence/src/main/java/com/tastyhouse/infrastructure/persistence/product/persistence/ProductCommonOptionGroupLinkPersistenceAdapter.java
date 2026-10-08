package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupLinkPersistencePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductCommonOptionGroupLinkJpaEntity.productCommonOptionGroupLinkJpaEntity;

@Repository
class ProductCommonOptionGroupLinkPersistenceAdapter implements ProductCommonOptionGroupLinkPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final ProductCommonOptionGroupLinkJpaRepository productCommonOptionGroupLinkJpaRepository;

    public ProductCommonOptionGroupLinkPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductCommonOptionGroupLinkJpaRepository productCommonOptionGroupLinkJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.productCommonOptionGroupLinkJpaRepository = productCommonOptionGroupLinkJpaRepository;
    }

    @Override
    public ProductCommonOptionGroupLink save(ProductCommonOptionGroupLink link) {
        if (link.getId() == null) {
            ProductCommonOptionGroupLinkJpaEntity saved =
                productCommonOptionGroupLinkJpaRepository.save(ProductCommonOptionGroupLinkMapper.toEntity(link));
            return ProductCommonOptionGroupLinkMapper.toDomain(saved);
        }

        ProductCommonOptionGroupLinkJpaEntity entity = productCommonOptionGroupLinkJpaRepository.findById(link.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 옵션그룹 연결입니다: " + link.getId()));
        ProductCommonOptionGroupLinkMapper.applyChanges(entity, link);
        return ProductCommonOptionGroupLinkMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductCommonOptionGroupLink> findByProductIdAndOptionGroupId(
        ProductId productId,
        ProductOptionGroupId optionGroupId
    ) {
        ProductCommonOptionGroupLinkJpaEntity entity = queryFactory
            .selectFrom(productCommonOptionGroupLinkJpaEntity)
            .where(
                productCommonOptionGroupLinkJpaEntity.productId.eq(productId.value()),
                productCommonOptionGroupLinkJpaEntity.optionGroupId.eq(optionGroupId.value())
            )
            .fetchOne();
        return Optional.ofNullable(entity).map(ProductCommonOptionGroupLinkMapper::toDomain);
    }

    @Override
    public List<ProductCommonOptionGroupLink> findAllByProductId(ProductId productId) {
        return queryFactory
            .selectFrom(productCommonOptionGroupLinkJpaEntity)
            .where(productCommonOptionGroupLinkJpaEntity.productId.eq(productId.value()))
            .orderBy(productCommonOptionGroupLinkJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ProductCommonOptionGroupLinkMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductCommonOptionGroupLink> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
        return queryFactory
            .selectFrom(productCommonOptionGroupLinkJpaEntity)
            .where(productCommonOptionGroupLinkJpaEntity.optionGroupId.eq(optionGroupId.value()))
            .fetch()
            .stream()
            .map(ProductCommonOptionGroupLinkMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductCommonOptionGroupLink> findAllByOptionGroupIdIn(List<ProductOptionGroupId> optionGroupIds) {
        if (optionGroupIds.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productCommonOptionGroupLinkJpaEntity)
            .where(productCommonOptionGroupLinkJpaEntity.optionGroupId.in(
                optionGroupIds.stream().map(ProductOptionGroupId::value).toList()
            ))
            .fetch()
            .stream()
            .map(ProductCommonOptionGroupLinkMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndOptionGroupId(ProductId productId, ProductOptionGroupId optionGroupId) {
        return queryFactory
            .selectOne()
            .from(productCommonOptionGroupLinkJpaEntity)
            .where(
                productCommonOptionGroupLinkJpaEntity.productId.eq(productId.value()),
                productCommonOptionGroupLinkJpaEntity.optionGroupId.eq(optionGroupId.value())
            )
            .fetchFirst() != null;
    }

    @Override
    public void delete(ProductCommonOptionGroupLink link) {
        productCommonOptionGroupLinkJpaRepository.deleteById(link.getId());
    }
}
