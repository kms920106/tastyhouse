package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.time.LocalDateTime;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.ProductFeedback;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackSavePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductFeedbackJpaEntity.productFeedbackJpaEntity;

@Repository
class ProductFeedbackPersistenceAdapter implements ProductFeedbackLoadPort, ProductFeedbackSavePort {

    private final JPAQueryFactory queryFactory;
    private final ProductFeedbackJpaRepository productFeedbackJpaRepository;

    public ProductFeedbackPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductFeedbackJpaRepository productFeedbackJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.productFeedbackJpaRepository = productFeedbackJpaRepository;
    }

    @Override
    public ProductFeedback save(ProductFeedback feedback) {
        ProductFeedbackJpaEntity saved = productFeedbackJpaRepository
            .save(ProductFeedbackMapper.toEntity(feedback));
        return ProductFeedbackMapper.toDomain(saved);
    }

    @Override
    public boolean existsRecentDuplicate(
        MemberId memberId,
        ProductId productId,
        ProductFeedbackType feedbackType,
        LocalDateTime since
    ) {
        return queryFactory
            .selectOne()
            .from(productFeedbackJpaEntity)
            .where(
                productFeedbackJpaEntity.memberId.eq(memberId.value()),
                productFeedbackJpaEntity.productId.eq(productId.value()),
                feedbackTypeEq(feedbackType == null ? null : feedbackType.name()),
                productFeedbackJpaEntity.createdAt.after(since)
            )
            .fetchFirst() != null;
    }

    @Override
    public boolean existsByShopIdAndCreatedAtAfter(ShopId shopId, LocalDateTime since) {
        return queryFactory
            .selectOne()
            .from(productFeedbackJpaEntity)
            .where(
                productFeedbackJpaEntity.shopId.eq(shopId.value()),
                productFeedbackJpaEntity.createdAt.after(since)
            )
            .fetchFirst() != null;
    }

    private BooleanExpression feedbackTypeEq(String feedbackType) {
        return feedbackType == null
            ? productFeedbackJpaEntity.feedbackType.isNull()
            : productFeedbackJpaEntity.feedbackType.eq(feedbackType);
    }
}
