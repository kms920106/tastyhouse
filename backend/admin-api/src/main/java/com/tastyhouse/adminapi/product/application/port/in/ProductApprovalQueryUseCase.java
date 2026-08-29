package com.tastyhouse.adminapi.product.application.port.in;

import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductImageChangeRequestItemResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductRepresentativeRequestItemResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.ProductVegetarianRequestItemResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 메뉴 변경 승인 요청 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ProductApprovalQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ProductApprovalQueryUseCase {

    PaginationResponse<ProductImageChangeRequestItemResponse> getImageChangeRequests(
        String status,
        int page,
        int size
    );

    PaginationResponse<ProductVegetarianRequestItemResponse> getVegetarianRequests(
        String status,
        int page,
        int size
    );

    PaginationResponse<ProductRepresentativeRequestItemResponse> getRepresentativeRequests(
        String status,
        int page,
        int size
    );
}
