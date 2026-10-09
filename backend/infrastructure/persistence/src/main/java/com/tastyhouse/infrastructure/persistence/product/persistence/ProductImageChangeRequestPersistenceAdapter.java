package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductImageChangeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.application.product.port.out.write.ProductImageChangeRequestLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductImageChangeRequestSavePort;

import static com.tastyhouse.infrastructure.persistence.product.persistence.QProductImageChangeRequestJpaEntity.productImageChangeRequestJpaEntity;

@Repository
class ProductImageChangeRequestPersistenceAdapter implements ProductImageChangeRequestLoadPort, ProductImageChangeRequestSavePort {

    private final JPAQueryFactory queryFactory;
    private final ProductImageChangeRequestJpaRepository productImageChangeRequestJpaRepository;

    public ProductImageChangeRequestPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductImageChangeRequestJpaRepository productImageChangeRequestJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.productImageChangeRequestJpaRepository = productImageChangeRequestJpaRepository;
    }

    @Override
    public ProductImageChangeRequest save(ProductImageChangeRequest request) {
        if (request.getId() == null) {
            ProductImageChangeRequestJpaEntity saved =
                productImageChangeRequestJpaRepository.save(ProductImageChangeRequestMapper.toEntity(request));
            return ProductImageChangeRequestMapper.toDomain(saved);
        }

        ProductImageChangeRequestJpaEntity entity = productImageChangeRequestJpaRepository.findById(request.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메뉴 이미지 변경 요청입니다: " + request.getId()));
        ProductImageChangeRequestMapper.applyChanges(entity, request);
        return ProductImageChangeRequestMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductImageChangeRequest> findById(ProductImageChangeRequestId id) {
        return productImageChangeRequestJpaRepository.findById(id.value())
            .map(ProductImageChangeRequestMapper::toDomain);
    }

    @Override
    public boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status) {
        return queryFactory
            .selectOne()
            .from(productImageChangeRequestJpaEntity)
            .where(
                productImageChangeRequestJpaEntity.productId.eq(productId.value()),
                statusEq(status == null ? null : status.name())
            )
            .fetchFirst() != null;
    }

    private BooleanExpression statusEq(String status) {
        return status == null
            ? productImageChangeRequestJpaEntity.status.isNull()
            : productImageChangeRequestJpaEntity.status.eq(status);
    }
}
