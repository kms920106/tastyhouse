package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductAllergenPersistencePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductAllergenJpaEntity.productAllergenJpaEntity;

@Repository
class ProductAllergenPersistenceAdapter implements ProductAllergenPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final ProductAllergenJpaRepository productAllergenJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ProductAllergenPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductAllergenJpaRepository productAllergenJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.productAllergenJpaRepository = productAllergenJpaRepository;
    }

    @Override
    public List<ProductAllergen> findAllByProductId(ProductId productId) {
        if (productId == null) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productAllergenJpaEntity)
            .where(productAllergenJpaEntity.productId.eq(productId.value()))
            .fetch()
            .stream()
            .map(ProductAllergenMapper::toDomain)
            .toList();
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
