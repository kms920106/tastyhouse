package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeExclusionLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupMergeExclusionSavePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductOptionGroupMergeExclusionJpaEntity.productOptionGroupMergeExclusionJpaEntity;

@Repository
class ProductOptionGroupMergeExclusionPersistenceAdapter
    implements ProductOptionGroupMergeExclusionLoadPort, ProductOptionGroupMergeExclusionSavePort {

    private final JPAQueryFactory queryFactory;
    private final ProductOptionGroupMergeExclusionJpaRepository jpaRepository;

    public ProductOptionGroupMergeExclusionPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductOptionGroupMergeExclusionJpaRepository jpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProductOptionGroupMergeExclusion save(ProductOptionGroupMergeExclusion exclusion) {
        ProductOptionGroupMergeExclusionJpaEntity saved =
            jpaRepository.save(ProductOptionGroupMergeExclusionMapper.toEntity(exclusion));
        return ProductOptionGroupMergeExclusionMapper.toDomain(saved);
    }

    @Override
    public Optional<ProductOptionGroupMergeExclusion> findByShopIdAndGroupSignature(
        ShopId shopId,
        String groupSignature
    ) {
        ProductOptionGroupMergeExclusionJpaEntity entity = queryFactory
            .selectFrom(productOptionGroupMergeExclusionJpaEntity)
            .where(
                productOptionGroupMergeExclusionJpaEntity.shopId.eq(shopId.value()),
                productOptionGroupMergeExclusionJpaEntity.groupSignature.eq(groupSignature)
            )
            .fetchOne();
        return Optional.ofNullable(entity).map(ProductOptionGroupMergeExclusionMapper::toDomain);
    }
}
