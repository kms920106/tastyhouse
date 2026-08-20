package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 메뉴 채식 설정 승인요청 write 포트.
 *
 * <p>{@code existsByProductIdAndStatus}는 "같은 메뉴에 PENDING 2건 금지" 불변식 검증용이다.
 */
public interface ProductVegetarianRequestRepository {

    ProductVegetarianRequest save(ProductVegetarianRequest request);

    Optional<ProductVegetarianRequest> findById(ProductVegetarianRequestId id);

    List<ProductVegetarianRequest> findAllByProductId(ProductId productId);

    boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status);
}
