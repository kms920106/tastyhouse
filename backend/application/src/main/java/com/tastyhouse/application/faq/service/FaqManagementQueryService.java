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
