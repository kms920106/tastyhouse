package com.tastyhouse.application.faq.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.faq.port.in.FaqManagementListQueryUseCase;
import com.tastyhouse.application.faq.port.out.FaqManagementListItemResult;
import com.tastyhouse.application.faq.port.out.FaqManagementQueryPort;
import com.tastyhouse.application.faq.port.out.FaqSearchCondition;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class FaqManagementListQueryService implements FaqManagementListQueryUseCase {

    private final FaqManagementQueryPort faqManagementQueryPort;

    public FaqManagementListQueryService(FaqManagementQueryPort faqManagementQueryPort) {
        this.faqManagementQueryPort = faqManagementQueryPort;
    }

    @Override
    public PageResult<FaqManagementListItemResult> getFaqs(Long categoryId, String question, Boolean visible, int page, int size) {
        FaqSearchCondition condition = FaqSearchCondition.of(categoryId, question, visible);
        PageQuery pageQuery = PageQuery.of(page, size);
        return faqManagementQueryPort.findAllFaqs(condition, pageQuery);
    }
}
