package com.tastyhouse.infrastructure.jpa.product.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductAllergenSavePort;

import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductAllergenJpaEntity.productAllergenJpaEntity;

@Repository
class ProductAllergenPersistenceAdapter implements ProductAllergenSavePort {

    private final JPAQueryFactory queryFactory;
    private final ProductAllergenJpaRepository productAllergenJpaRepository;
    private final EntityManager entityManager;

    public ProductAllergenPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductAllergenJpaRepository productAllergenJpaRepository,
        EntityManager entityManager
    ) {
        this.queryFactory = queryFactory;
        this.productAllergenJpaRepository = productAllergenJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public List<ProductAllergen> saveAll(List<ProductAllergen> productAllergens) {
        List<ProductAllergenJpaEntity> entities = productAllergens.stream()
            .map(ProductAllergenMapper::toEntity)
            .toList();
        return productAllergenJpaRepository.saveAll(entities).stream()
            .map(ProductAllergenMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteAllByProductId(ProductId productId) {
        entityManager.flush();
        if (productId != null) {
            queryFactory
                .delete(productAllergenJpaEntity)
                .where(productAllergenJpaEntity.productId.eq(productId.value()))
                .execute();
        }
        entityManager.clear();
    }
}
