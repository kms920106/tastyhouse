package com.tastyhouse.adminapi.product.application.port.in;

import com.tastyhouse.adminapi.product.adapter.in.web.response.StorePriceVerificationDetailResponse;
import com.tastyhouse.adminapi.product.adapter.in.web.response.StorePriceVerificationListItemResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 매장 가격 검증 제보 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code StorePriceVerificationQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface StorePriceVerificationQueryUseCase {

    PaginationResponse<StorePriceVerificationListItemResponse> getVerifications(
        String status,
        int page,
        int size
    );

    StorePriceVerificationDetailResponse getVerification(Long verificationId);
}
