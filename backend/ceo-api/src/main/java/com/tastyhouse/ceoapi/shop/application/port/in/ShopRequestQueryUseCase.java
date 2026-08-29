package com.tastyhouse.ceoapi.shop.application.port.in;

import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopRequestCommentResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopRequestDetailResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopRequestListItemResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopRequestTypeCatalogResponse;

/**
 * 가게 요청 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopRequestQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ShopRequestQueryUseCase {

    PaginationResponse<ShopRequestListItemResponse> getRequests(
        Long ceoId,
        Long shopId,
        String requestType,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    );

    ShopRequestDetailResponse getRequestDetail(Long ceoId, Long shopId, Long requestId);

    List<ShopRequestCommentResponse> getComments(Long ceoId, Long shopId, Long requestId);

    ShopRequestTypeCatalogResponse getRequestTypes();
}
