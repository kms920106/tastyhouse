package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductFeedbackRead;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackReadLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackReadSavePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductFeedbackReadJpaEntity.productFeedbackReadJpaEntity;

@Repository
class ProductFeedbackReadPersistenceAdapter implements ProductFeedbackReadLoadPort, ProductFeedbackReadSavePort {

    private final JPAQueryFactory queryFactory;
    private final ProductFeedbackReadJpaRepository productFeedbackReadJpaRepository;

    public ProductFeedbackReadPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductFeedbackReadJpaRepository productFeedbackReadJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.productFeedbackReadJpaRepository = productFeedbackReadJpaRepository;
    }

    @Override
    public ProductFeedbackRead save(ProductFeedbackRead feedbackRead) {
        if (feedbackRead.getId() == null) {
            ProductFeedbackReadJpaEntity saved = productFeedbackReadJpaRepository
                .save(ProductFeedbackReadMapper.toEntity(feedbackRead));
            return ProductFeedbackReadMapper.toDomain(saved);
        }

        ProductFeedbackReadJpaEntity entity = productFeedbackReadJpaRepository
            .findById(feedbackRead.getId())
            .orElseThrow(() -> new IllegalStateException(
                "존재하지 않는 고객 의견 확인 이력입니다: " + feedbackRead.getId()));
        ProductFeedbackReadMapper.applyChanges(entity, feedbackRead);
        return ProductFeedbackReadMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductFeedbackRead> findByShopId(ShopId shopId) {
        ProductFeedbackReadJpaEntity entity = queryFactory
            .selectFrom(productFeedbackReadJpaEntity)
            .where(productFeedbackReadJpaEntity.shopId.eq(shopId.value()))
            .fetchOne();
        return Optional.ofNullable(entity).map(ProductFeedbackReadMapper::toDomain);
    }
}
