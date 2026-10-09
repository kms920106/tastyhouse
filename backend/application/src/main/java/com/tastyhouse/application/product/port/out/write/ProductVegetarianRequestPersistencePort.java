package com.tastyhouse.application.product.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

public interface ProductVegetarianRequestPersistencePort {

    ProductVegetarianRequest save(ProductVegetarianRequest request);

    Optional<ProductVegetarianRequest> findById(ProductVegetarianRequestId id);

    boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status);
}
