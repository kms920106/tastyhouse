package com.tastyhouse.adminapi.shop.application.port.in;

import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopContentBoardListItemResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 가게 콘텐츠 게시판 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopContentBoardQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ShopContentBoardQueryUseCase {

    PaginationResponse<ShopContentBoardListItemResponse> getContentBoards(
        Long shopId,
        Boolean hidden,
        String contentType,
        int page,
        int size
    );
}
