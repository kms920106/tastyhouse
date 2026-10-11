package com.tastyhouse.application.product.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductImageChangeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

public interface ProductImageChangeRequestLoadPort {

    Optional<ProductImageChangeRequest> findById(ProductImageChangeRequestId id);

    boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status);
}
