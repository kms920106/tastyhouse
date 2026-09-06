package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductImageChangeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

public interface ProductImageChangeRequestRepository {
    ProductImageChangeRequest save(ProductImageChangeRequest request);

    Optional<ProductImageChangeRequest> findById(ProductImageChangeRequestId id);

    List<ProductImageChangeRequest> findAllByProductId(ProductId productId);

    boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status);
}
