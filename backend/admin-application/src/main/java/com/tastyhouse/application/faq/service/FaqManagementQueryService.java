package com.tastyhouse.application.faq.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.faq.port.in.FaqManagementQueryUseCase;
import com.tastyhouse.application.faq.port.out.FaqCategoryManagementResult;
import com.tastyhouse.application.faq.port.out.FaqDetailResult;
import com.tastyhouse.application.faq.port.out.FaqManagementListItemResult;
import com.tastyhouse.application.faq.port.out.FaqManagementQueryPort;
import com.tastyhouse.application.faq.port.out.FaqSearchCondition;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class FaqManagementQueryService implements FaqManagementQueryUseCase {

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
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.FAQ_CATEGORY_NOT_FOUND));
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
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.FAQ_NOT_FOUND));
    }
}
