package com.tastyhouse.adminapplication.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.product.port.out.ProductImageChangeRequestResult;
import com.tastyhouse.application.product.port.out.ProductManagementQueryPort;
import com.tastyhouse.application.product.port.out.ProductRepresentativeRequestResult;
import com.tastyhouse.application.product.port.out.ProductVegetarianRequestResult;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapplication.product.response.ProductImageChangeRequestItemResponse;
import com.tastyhouse.adminapplication.product.response.ProductRepresentativeRequestItemResponse;
import com.tastyhouse.adminapplication.product.response.ProductVegetarianRequestItemResponse;
import com.tastyhouse.adminapplication.product.port.in.ProductApprovalQueryUseCase;

/**
 * 메뉴 이미지·채식·사장님 추천 승인요청 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 요청을 승인 상태로 필터해 조회한다 — 관리자는 모든 가게의 요청을 본다.
 */
@Service
@Transactional(readOnly = true)
public class ProductApprovalQueryService implements ProductApprovalQueryUseCase {

    private final ProductManagementQueryPort productManagementQueryPort;

    public ProductApprovalQueryService(ProductManagementQueryPort productManagementQueryPort) {
        this.productManagementQueryPort = productManagementQueryPort;
    }

    @Override
    public PaginationResponse<ProductImageChangeRequestItemResponse> getImageChangeRequests(
        String status,
        int page,
        int size
    ) {
        ApprovalStatus approvalStatus = promoteStatus(status);

        PageResult<ProductImageChangeRequestResult> pageResult = productManagementQueryPort
            .findImageChangeRequestPage(approvalStatus, PageQuery.of(page, size));

        return PaginationResponse.from(pageResult.map(this::toProductImageChangeRequestItemResponse));
    }

    @Override
    public PaginationResponse<ProductVegetarianRequestItemResponse> getVegetarianRequests(
        String status,
        int page,
        int size
    ) {
        ApprovalStatus approvalStatus = promoteStatus(status);

        PageResult<ProductVegetarianRequestResult> pageResult = productManagementQueryPort
            .findVegetarianRequestPage(approvalStatus, PageQuery.of(page, size));

        return PaginationResponse.from(pageResult.map(this::toProductVegetarianRequestItemResponse));
    }

    @Override
    public PaginationResponse<ProductRepresentativeRequestItemResponse> getRepresentativeRequests(
        String status,
        int page,
        int size
    ) {
        ApprovalStatus approvalStatus = promoteStatus(status);

        PageResult<ProductRepresentativeRequestResult> pageResult = productManagementQueryPort
            .findRepresentativeRequestPage(approvalStatus, PageQuery.of(page, size));

        return PaginationResponse.from(pageResult.map(this::toProductRepresentativeRequestItemResponse));
    }

    /** 상태 미지정({@code null})은 "전체"를 뜻하므로 승격하지 않는다. */
    private ApprovalStatus promoteStatus(String status) {
        return status == null ? null : ApprovalStatus.valueOf(status);
    }

    private ProductImageChangeRequestItemResponse toProductImageChangeRequestItemResponse(
        ProductImageChangeRequestResult dto
    ) {
        return ProductImageChangeRequestItemResponse.from(
            dto.id(),
            dto.productId(),
            dto.shopId(),
            dto.productName(),
            dto.imageUrl(),
            dto.status().name(),
            dto.rejectReason()
        );
    }

    private ProductVegetarianRequestItemResponse toProductVegetarianRequestItemResponse(
        ProductVegetarianRequestResult dto
    ) {
        return ProductVegetarianRequestItemResponse.from(
            dto.id(),
            dto.productId(),
            dto.shopId(),
            dto.productName(),
            dto.vegetarianType().name(),
            dto.ingredients(),
            dto.description(),
            dto.status().name(),
            dto.rejectReason()
        );
    }

    private ProductRepresentativeRequestItemResponse toProductRepresentativeRequestItemResponse(
        ProductRepresentativeRequestResult dto
    ) {
        return ProductRepresentativeRequestItemResponse.from(
            dto.id(),
            dto.productId(),
            dto.shopId(),
            dto.shopName(),
            dto.productName(),
            dto.imageUrl(),
            dto.status().name(),
            dto.rejectReason()
        );
    }
}
