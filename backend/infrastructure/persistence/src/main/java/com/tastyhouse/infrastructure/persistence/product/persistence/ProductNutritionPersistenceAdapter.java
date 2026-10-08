package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductNutritionPersistencePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductNutritionJpaEntity.productNutritionJpaEntity;

@Repository
class ProductNutritionPersistenceAdapter implements ProductNutritionPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final ProductNutritionJpaRepository productNutritionJpaRepository;

    public ProductNutritionPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductNutritionJpaRepository productNutritionJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.productNutritionJpaRepository = productNutritionJpaRepository;
    }

    @Override
    public Optional<ProductNutrition> findByProductId(ProductId productId) {
        if (productId == null) {
            return Optional.empty();
        }
        ProductNutritionJpaEntity entity = queryFactory
            .selectFrom(productNutritionJpaEntity)
            .where(productNutritionJpaEntity.productId.eq(productId.value()))
            .fetchOne();
        return Optional.ofNullable(entity).map(ProductNutritionMapper::toDomain);
    }

    @Override
    public ProductNutrition save(ProductNutrition productNutrition) {
        if (productNutrition.getId() == null) {
            ProductNutritionJpaEntity saved =
                productNutritionJpaRepository.save(ProductNutritionMapper.toEntity(productNutrition));
            return ProductNutritionMapper.toDomain(saved);
        }

        ProductNutritionJpaEntity entity = productNutritionJpaRepository.findById(productNutrition.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메뉴 영양성분입니다: " + productNutrition.getId()));
        ProductNutritionMapper.applyChanges(entity, productNutrition);
        return ProductNutritionMapper.toDomain(entity);
    }

    @Override
    public void delete(ProductNutrition productNutrition) {
        productNutritionJpaRepository.deleteById(productNutrition.getId());
    }
}
