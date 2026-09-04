package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import com.tastyhouse.application.faq.port.out.FaqCategoryResult;
import com.tastyhouse.application.faq.port.out.FaqResult;

/**
 * FAQ 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code FaqQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
@WebApp
public interface FaqQueryUseCase {

    List<FaqCategoryResult> getFaqCategories();

    List<FaqResult> getFaqList(Long categoryId);
}
