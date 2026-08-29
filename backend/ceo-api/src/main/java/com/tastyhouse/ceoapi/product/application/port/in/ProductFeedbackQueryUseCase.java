package com.tastyhouse.ceoapi.product.application.port.in;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductFeedbackResponse;
import com.tastyhouse.ceoapi.product.adapter.in.web.response.ProductFeedbackUnreadResponse;

/**
 * 메뉴 고객 의견 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ProductFeedbackQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface ProductFeedbackQueryUseCase {

    PaginationResponse<ProductFeedbackResponse> getFeedbacks(Long ceoId, Long shopId, int page, int size);

    ProductFeedbackUnreadResponse getUnread(Long ceoId, Long shopId);
}
