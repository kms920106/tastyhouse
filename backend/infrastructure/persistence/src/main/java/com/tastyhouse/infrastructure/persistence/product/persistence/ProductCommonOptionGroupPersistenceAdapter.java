package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupPersistencePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductCommonOptionGroupJpaEntity.productCommonOptionGroupJpaEntity;

@Repository
class ProductCommonOptionGroupPersistenceAdapter implements ProductCommonOptionGroupPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final ProductCommonOptionGroupJpaRepository productCommonOptionGroupJpaRepository;

    public ProductCommonOptionGroupPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductCommonOptionGroupJpaRepository productCommonOptionGroupJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.productCommonOptionGroupJpaRepository = productCommonOptionGroupJpaRepository;
    }

    @Override
    public ProductCommonOptionGroup save(ProductCommonOptionGroup productCommonOptionGroup) {
        if (productCommonOptionGroup.getId() == null) {
            ProductCommonOptionGroupJpaEntity saved =
                productCommonOptionGroupJpaRepository.save(ProductCommonOptionGroupMapper.toEntity(productCommonOptionGroup));
            return ProductCommonOptionGroupMapper.toDomain(saved);
        }

        ProductCommonOptionGroupJpaEntity jpaEntity = productCommonOptionGroupJpaRepository.findById(productCommonOptionGroup.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 공통 옵션 그룹입니다: " + productCommonOptionGroup.getId()));
        ProductCommonOptionGroupMapper.applyChanges(jpaEntity, productCommonOptionGroup);
        return ProductCommonOptionGroupMapper.toDomain(jpaEntity);
    }

    @Override
    public List<ProductCommonOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productCommonOptionGroupJpaEntity)
            .where(productCommonOptionGroupJpaEntity.id.in(ids.stream().map(ProductOptionGroupId::value).toList()))
            .fetch()
            .stream()
            .map(ProductCommonOptionGroupMapper::toDomain)
            .toList();
    }
}
