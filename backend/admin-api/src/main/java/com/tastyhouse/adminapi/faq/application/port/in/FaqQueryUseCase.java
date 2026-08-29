package com.tastyhouse.adminapi.faq.application.port.in;

import java.util.List;

import com.tastyhouse.adminapi.faq.adapter.in.web.response.FaqCategoryResponse;
import com.tastyhouse.adminapi.faq.adapter.in.web.response.FaqDetailResponse;
import com.tastyhouse.adminapi.faq.adapter.in.web.response.FaqListItemResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * FAQ 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code FaqQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface FaqQueryUseCase {

    List<FaqCategoryResponse> getCategories();

    FaqCategoryResponse getCategory(Long categoryId);

    PaginationResponse<FaqListItemResponse> getFaqs(Long categoryId, String question, Boolean visible, int page, int size);

    FaqDetailResponse getFaq(Long id);
}
