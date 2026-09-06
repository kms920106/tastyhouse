package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

public interface ProductVegetarianRequestRepository {
    ProductVegetarianRequest save(ProductVegetarianRequest request);

    Optional<ProductVegetarianRequest> findById(ProductVegetarianRequestId id);

    List<ProductVegetarianRequest> findAllByProductId(ProductId productId);

    boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status);
}
