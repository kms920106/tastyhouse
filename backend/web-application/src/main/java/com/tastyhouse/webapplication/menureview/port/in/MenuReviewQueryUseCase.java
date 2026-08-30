package com.tastyhouse.webapplication.menureview.port.in;

import java.util.List;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapplication.menureview.response.MenuReviewListItemResponse;
import com.tastyhouse.webapplication.menureview.response.MenuReviewWritableItemResponse;

/**
 * 메뉴 리뷰 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code MenuReviewQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface MenuReviewQueryUseCase {

    List<MenuReviewWritableItemResponse> findWritableItems(Long orderId, Long memberId);

    PaginationResponse<MenuReviewListItemResponse> findByProductId(Long productId, int page, int size);
}
