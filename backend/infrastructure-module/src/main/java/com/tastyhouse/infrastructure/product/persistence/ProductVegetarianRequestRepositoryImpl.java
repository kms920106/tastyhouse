package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.repository.ProductVegetarianRequestRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 메뉴 채식 설정 승인요청 write 어댑터. 표현 목적 조회는 {@code ProductQueryDao}가 담당한다.
 */
@Repository
public class ProductVegetarianRequestRepositoryImpl implements ProductVegetarianRequestRepository {

    private final ProductVegetarianRequestJpaRepository productVegetarianRequestJpaRepository;

    public ProductVegetarianRequestRepositoryImpl(
        ProductVegetarianRequestJpaRepository productVegetarianRequestJpaRepository
    ) {
        this.productVegetarianRequestJpaRepository = productVegetarianRequestJpaRepository;
    }

    @Override
    public ProductVegetarianRequest save(ProductVegetarianRequest request) {
        if (request.getId() == null) {
            ProductVegetarianRequestJpaEntity saved =
                productVegetarianRequestJpaRepository.save(ProductVegetarianRequestMapper.toEntity(request));
            return ProductVegetarianRequestMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
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
    public List<ProductVegetarianRequest> findAllByProductId(ProductId productId) {
        return productVegetarianRequestJpaRepository.findAllByProductId(productId.value()).stream()
            .map(ProductVegetarianRequestMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status) {
        return productVegetarianRequestJpaRepository.existsByProductIdAndStatus(productId.value(), status);
    }
}
