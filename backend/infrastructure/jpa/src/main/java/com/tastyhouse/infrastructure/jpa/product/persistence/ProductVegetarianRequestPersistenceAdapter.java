package com.tastyhouse.infrastructure.jpa.product.persistence;

import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.application.product.port.out.write.ProductVegetarianRequestLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductVegetarianRequestSavePort;

import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductVegetarianRequestJpaEntity.productVegetarianRequestJpaEntity;

@Repository
class ProductVegetarianRequestPersistenceAdapter implements ProductVegetarianRequestLoadPort, ProductVegetarianRequestSavePort {

    private final JPAQueryFactory queryFactory;
    private final ProductVegetarianRequestJpaRepository productVegetarianRequestJpaRepository;

    public ProductVegetarianRequestPersistenceAdapter(
        JPAQueryFactory queryFactory,
        ProductVegetarianRequestJpaRepository productVegetarianRequestJpaRepository
    ) {
        this.queryFactory = queryFactory;
        this.productVegetarianRequestJpaRepository = productVegetarianRequestJpaRepository;
    }

    @Override
    public ProductVegetarianRequest save(ProductVegetarianRequest request) {
        if (request.getId() == null) {
            ProductVegetarianRequestJpaEntity saved =
                productVegetarianRequestJpaRepository.save(ProductVegetarianRequestMapper.toEntity(request));
            return ProductVegetarianRequestMapper.toDomain(saved);
        }

        ProductVegetarianRequestJpaEntity entity = productVegetarianRequestJpaRepository.findById(request.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메뉴 채식 설정 요청입니다: " + request.getId()));
        ProductVegetarianRequestMapper.applyChanges(entity, request);
        return ProductVegetarianRequestMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductVegetarianRequest> findById(ProductVegetarianRequestId id) {
        return productVegetarianRequestJpaRepository.findById(id.value())
            .map(ProductVegetarianRequestMapper::toDomain);
    }

    @Override
    public boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status) {
        return queryFactory
            .selectOne()
            .from(productVegetarianRequestJpaEntity)
            .where(
                productVegetarianRequestJpaEntity.productId.eq(productId.value()),
                statusEq(status == null ? null : status.name())
            )
            .fetchFirst() != null;
    }

    private BooleanExpression statusEq(String status) {
        return status == null
            ? productVegetarianRequestJpaEntity.status.isNull()
            : productVegetarianRequestJpaEntity.status.eq(status);
    }
}
