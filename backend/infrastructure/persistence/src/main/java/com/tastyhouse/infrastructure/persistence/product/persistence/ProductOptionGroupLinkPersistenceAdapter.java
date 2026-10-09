package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkSavePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductOptionGroupLinkJpaEntity.productOptionGroupLinkJpaEntity;

@Repository
class ProductOptionGroupLinkPersistenceAdapter implements ProductOptionGroupLinkLoadPort, ProductOptionGroupLinkSavePort {

    private final JPAQueryFactory queryFactory;
    private final ProductOptionGroupLinkJpaRepository productOptionGroupLinkJpaRepository;

    public ProductOptionGroupLinkPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductOptionGroupLinkJpaRepository productOptionGroupLinkJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.productOptionGroupLinkJpaRepository = productOptionGroupLinkJpaRepository;
    }

    @Override
    public ProductOptionGroupLink save(ProductOptionGroupLink link) {
        if (link.getId() == null) {
            ProductOptionGroupLinkJpaEntity saved =
                productOptionGroupLinkJpaRepository.save(ProductOptionGroupLinkMapper.toEntity(link));
            return ProductOptionGroupLinkMapper.toDomain(saved);
        }

        ProductOptionGroupLinkJpaEntity entity = productOptionGroupLinkJpaRepository.findById(link.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 옵션그룹 연결입니다: " + link.getId()));
        ProductOptionGroupLinkMapper.applyChanges(entity, link);
        return ProductOptionGroupLinkMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductOptionGroupLink> findByProductIdAndOptionGroupId(
        ProductId productId,
        ProductOptionGroupId optionGroupId
    ) {
        ProductOptionGroupLinkJpaEntity entity = queryFactory
            .selectFrom(productOptionGroupLinkJpaEntity)
            .where(
                productOptionGroupLinkJpaEntity.productId.eq(productId.value()),
                productOptionGroupLinkJpaEntity.optionGroupId.eq(optionGroupId.value())
            )
            .fetchOne();
        return Optional.ofNullable(entity).map(ProductOptionGroupLinkMapper::toDomain);
    }

    @Override
    public List<ProductOptionGroupLink> findAllByProductId(ProductId productId) {
        return queryFactory
            .selectFrom(productOptionGroupLinkJpaEntity)
            .where(productOptionGroupLinkJpaEntity.productId.eq(productId.value()))
            .orderBy(productOptionGroupLinkJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(ProductOptionGroupLinkMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductOptionGroupLink> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
        return queryFactory
            .selectFrom(productOptionGroupLinkJpaEntity)
            .where(productOptionGroupLinkJpaEntity.optionGroupId.eq(optionGroupId.value()))
            .fetch()
            .stream()
            .map(ProductOptionGroupLinkMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductOptionGroupLink> findAllByOptionGroupIdIn(List<ProductOptionGroupId> optionGroupIds) {
        if (optionGroupIds.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productOptionGroupLinkJpaEntity)
            .where(productOptionGroupLinkJpaEntity.optionGroupId.in(
                optionGroupIds.stream().map(ProductOptionGroupId::value).toList()
            ))
            .fetch()
            .stream()
            .map(ProductOptionGroupLinkMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndOptionGroupId(ProductId productId, ProductOptionGroupId optionGroupId) {
        return queryFactory
            .selectOne()
            .from(productOptionGroupLinkJpaEntity)
            .where(
                productOptionGroupLinkJpaEntity.productId.eq(productId.value()),
                productOptionGroupLinkJpaEntity.optionGroupId.eq(optionGroupId.value())
            )
            .fetchFirst() != null;
    }

    @Override
    public void delete(ProductOptionGroupLink link) {
        productOptionGroupLinkJpaRepository.deleteById(link.getId());
    }
}
