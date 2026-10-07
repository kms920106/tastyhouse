package com.tastyhouse.application.faq.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.faq.port.in.FaqManagementCategoryDetailQueryUseCase;
import com.tastyhouse.application.faq.port.out.FaqCategoryManagementResult;
import com.tastyhouse.application.faq.port.out.FaqManagementQueryPort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class FaqManagementCategoryDetailQueryService implements FaqManagementCategoryDetailQueryUseCase {

    private final FaqManagementQueryPort faqManagementQueryPort;

    public FaqManagementCategoryDetailQueryService(FaqManagementQueryPort faqManagementQueryPort) {
        this.faqManagementQueryPort = faqManagementQueryPort;
    }

    @Override
    public FaqCategoryManagementResult getCategory(Long categoryId) {
        return faqManagementQueryPort.findCategoryDetailById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
