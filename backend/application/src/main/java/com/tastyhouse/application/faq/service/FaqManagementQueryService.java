package com.tastyhouse.application.faq.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.faq.port.out.FaqCategoryManagementResult;
import com.tastyhouse.application.faq.port.out.FaqDetailResult;
import com.tastyhouse.application.faq.port.out.FaqManagementListItemResult;
import com.tastyhouse.application.faq.port.out.FaqManagementQueryPort;
import com.tastyhouse.application.faq.port.out.FaqSearchCondition;
import com.tastyhouse.application.faq.port.in.FaqManagementQueryUseCase;

/**
 * FAQ 관리 조회 서비스.
 *
 * <p>읽기 포트({@link FaqManagementQueryPort})만 주입해 조회한다. write 포트를 주입하지 않으며,
 * 쓰기는 {@link FaqCommandService}·{@link FaqCategoryCommandService}가 담당한다. 항목과 카테고리
 * 조회를 한 서비스에 두는 것은 DAO가 도메인당 1개인 것과 같은 이유로, 두 애그리거트가 같은 관리
 * 화면을 구성하기 때문이다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@AdminApp
@Transactional(readOnly = true)
public class FaqManagementQueryService implements FaqManagementQueryUseCase {

    private final FaqManagementQueryPort faqManagementQueryPort;

    public FaqManagementQueryService(FaqManagementQueryPort faqManagementQueryPort) {
        this.faqManagementQueryPort = faqManagementQueryPort;
    }

    @Override
    public List<FaqCategoryManagementResult> getCategories() {
        return faqManagementQueryPort.findAllCategories();
    }

    @Override
    public FaqCategoryManagementResult getCategory(Long categoryId) {
        return faqManagementQueryPort.findCategoryDetailById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }

    @Override
    public PageResult<FaqManagementListItemResult> getFaqs(Long categoryId, String question, Boolean visible, int page, int size) {
        FaqSearchCondition condition = FaqSearchCondition.of(categoryId, question, visible);
        PageQuery pageQuery = PageQuery.of(page, size);
        return faqManagementQueryPort.findAllFaqs(condition, pageQuery);
    }

    @Override
    public FaqDetailResult getFaq(Long id) {
        return faqManagementQueryPort.findFaqDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FAQ_NOT_FOUND));
    }
}
