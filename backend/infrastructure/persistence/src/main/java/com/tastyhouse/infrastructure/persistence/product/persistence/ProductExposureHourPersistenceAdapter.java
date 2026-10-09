package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductExposureHour;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductExposureHourPersistencePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductExposureHourJpaEntity.productExposureHourJpaEntity;

@Repository
class ProductExposureHourPersistenceAdapter implements ProductExposureHourPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final ProductExposureHourJpaRepository productExposureHourJpaRepository;
    private final EntityManager entityManager;

    public ProductExposureHourPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductExposureHourJpaRepository productExposureHourJpaRepository,
        EntityManager entityManager
    ) {
        this.queryFactory = queryFactory;
        this.productExposureHourJpaRepository = productExposureHourJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public List<ProductExposureHour> saveAll(List<ProductExposureHour> hours) {
        if (hours.isEmpty()) {
            return List.of();
        }
        List<ProductExposureHourJpaEntity> entities = hours.stream()
            .map(ProductExposureHourMapper::toEntity)
            .toList();
        return productExposureHourJpaRepository.saveAll(entities).stream()
            .map(ProductExposureHourMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductExposureHour> findAllByProductId(ProductId productId) {
        return queryFactory
            .selectFrom(productExposureHourJpaEntity)
            .where(productExposureHourJpaEntity.productId.eq(productId.value()))
            .fetch()
            .stream()
            .map(ProductExposureHourMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteAllByProductId(ProductId productId) {
        entityManager.flush();
        queryFactory
            .delete(productExposureHourJpaEntity)
            .where(productExposureHourJpaEntity.productId.eq(productId.value()))
            .execute();
        entityManager.clear();
    }
}
