package com.tastyhouse.adminapi.shop.application.port.in;

import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopDeliveryAreaAdjustmentDetailResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopDeliveryAreaAdjustmentListItemResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 가게 배달 지역 조정 요청 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopDeliveryAreaAdjustmentQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ShopDeliveryAreaAdjustmentQueryUseCase {

    PaginationResponse<ShopDeliveryAreaAdjustmentListItemResponse> getAdjustmentRequests(
        String status,
        Long shopId,
        int page,
        int size
    );

    ShopDeliveryAreaAdjustmentDetailResponse getAdjustmentRequest(Long requestId);
}
