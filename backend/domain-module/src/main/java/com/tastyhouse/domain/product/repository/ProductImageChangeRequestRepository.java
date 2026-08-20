package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductImageChangeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

/**
 * 메뉴 이미지 변경 승인요청 write 포트.
 *
 * <p>{@code existsByProductIdAndStatus}는 "같은 메뉴에 PENDING 2건 금지" 불변식 검증용이므로
 * 화면용 집계가 아니라 write 포트에 남긴다.
 */
public interface ProductImageChangeRequestRepository {

    ProductImageChangeRequest save(ProductImageChangeRequest request);

    Optional<ProductImageChangeRequest> findById(ProductImageChangeRequestId id);

    List<ProductImageChangeRequest> findAllByProductId(ProductId productId);

    boolean existsByProductIdAndStatus(ProductId productId, ApprovalStatus status);
}
