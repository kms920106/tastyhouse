package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.product.port.out.ProductFeedbackSummaryResult;

/**
 * 메뉴 고객 의견 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ProductFeedbackQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
@CeoApp
public interface ProductFeedbackQueryUseCase {

    PageResult<ProductFeedbackSummaryResult> getFeedbacks(Long ceoId, Long shopId, int page, int size);

    boolean getUnread(Long ceoId, Long shopId);
}
