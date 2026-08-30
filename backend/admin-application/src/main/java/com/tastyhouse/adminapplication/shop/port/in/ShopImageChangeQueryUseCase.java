package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.adminapplication.shop.response.ShopImageChangeRequestItemResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 가게 이미지 변경 요청 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopImageChangeQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ShopImageChangeQueryUseCase {

    PaginationResponse<ShopImageChangeRequestItemResponse> getImageChangeRequests(
        String status,
        String imageType,
        int page,
        int size
    );
}
