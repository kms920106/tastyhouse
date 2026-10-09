package com.tastyhouse.infrastructure.jpa.product.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupSavePort;

import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductOptionGroupJpaEntity.productOptionGroupJpaEntity;

@Repository
class ProductOptionGroupPersistenceAdapter implements ProductOptionGroupLoadPort, ProductOptionGroupSavePort {

    private final JPAQueryFactory queryFactory;
    private final ProductOptionGroupJpaRepository productOptionGroupJpaRepository;

    public ProductOptionGroupPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductOptionGroupJpaRepository productOptionGroupJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.productOptionGroupJpaRepository = productOptionGroupJpaRepository;
    }

    @Override
    public Optional<ProductOptionGroup> findById(ProductOptionGroupId id) {
        return productOptionGroupJpaRepository.findById(id.value()).map(ProductOptionGroupMapper::toDomain);
    }

    @Override
    public ProductOptionGroup save(ProductOptionGroup productOptionGroup) {
        if (productOptionGroup.getId() == null) {
            ProductOptionGroupJpaEntity saved =
                productOptionGroupJpaRepository.save(ProductOptionGroupMapper.toEntity(productOptionGroup));
            return ProductOptionGroupMapper.toDomain(saved);
        }

        ProductOptionGroupJpaEntity jpaEntity = productOptionGroupJpaRepository.findById(productOptionGroup.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품 옵션 그룹입니다: " + productOptionGroup.getId()));
        ProductOptionGroupMapper.applyChanges(jpaEntity, productOptionGroup);
        return ProductOptionGroupMapper.toDomain(jpaEntity);
    }

    @Override
    public List<ProductOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productOptionGroupJpaEntity)
            .where(productOptionGroupJpaEntity.id.in(ids.stream().map(ProductOptionGroupId::value).toList()))
            .fetch()
            .stream()
            .map(ProductOptionGroupMapper::toDomain)
            .toList();
    }
}
