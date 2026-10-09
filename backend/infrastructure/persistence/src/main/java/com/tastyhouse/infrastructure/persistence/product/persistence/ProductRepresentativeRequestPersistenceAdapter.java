package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductRepresentativeRequestLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductRepresentativeRequestSavePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductRepresentativeRequestJpaEntity.productRepresentativeRequestJpaEntity;

@Repository
class ProductRepresentativeRequestPersistenceAdapter implements ProductRepresentativeRequestLoadPort, ProductRepresentativeRequestSavePort {

    private final JPAQueryFactory queryFactory;
    private final ProductRepresentativeRequestJpaRepository productRepresentativeRequestJpaRepository;

    public ProductRepresentativeRequestPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductRepresentativeRequestJpaRepository productRepresentativeRequestJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.productRepresentativeRequestJpaRepository = productRepresentativeRequestJpaRepository;
    }

    @Override
    public ProductRepresentativeRequest save(ProductRepresentativeRequest request) {
        if (request.getId() == null) {
            ProductRepresentativeRequestJpaEntity saved = productRepresentativeRequestJpaRepository
                .save(ProductRepresentativeRequestMapper.toEntity(request));
            return ProductRepresentativeRequestMapper.toDomain(saved);
        }

        ProductRepresentativeRequestJpaEntity entity = productRepresentativeRequestJpaRepository
            .findById(request.getId())
            .orElseThrow(() -> new IllegalStateException(
                "존재하지 않는 사장님 추천 지정 요청입니다: " + request.getId()));
        ProductRepresentativeRequestMapper.applyChanges(entity, request);
        return ProductRepresentativeRequestMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductRepresentativeRequest> findById(ProductRepresentativeRequestId id) {
        return productRepresentativeRequestJpaRepository.findById(id.value())
            .map(ProductRepresentativeRequestMapper::toDomain);
    }

    @Override
    public boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status) {
        return queryFactory
            .selectOne()
            .from(productRepresentativeRequestJpaEntity)
            .where(
                productRepresentativeRequestJpaEntity.productId.eq(productId.value()),
                statusEq(status == null ? null : status.name())
            )
            .fetchFirst() != null;
    }

    @Override
    public long countByShopIdAndStatus(ShopId shopId, ApprovalStatus status) {
        Long count = queryFactory
            .select(productRepresentativeRequestJpaEntity.count())
            .from(productRepresentativeRequestJpaEntity)
            .where(
                productRepresentativeRequestJpaEntity.shopId.eq(shopId.value()),
                statusEq(status == null ? null : status.name())
            )
            .fetchOne();
        return count == null ? 0L : count;
    }

    private BooleanExpression statusEq(String status) {
        return status == null
            ? productRepresentativeRequestJpaEntity.status.isNull()
            : productRepresentativeRequestJpaEntity.status.eq(status);
    }
}
