package com.tastyhouse.application.faq.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.faq.port.in.FaqManagementCategoryListQueryUseCase;
import com.tastyhouse.application.faq.port.out.FaqCategoryManagementResult;
import com.tastyhouse.application.faq.port.out.FaqManagementQueryPort;

@Service
@Transactional(readOnly = true)
class FaqManagementCategoryListQueryService implements FaqManagementCategoryListQueryUseCase {

    private final FaqManagementQueryPort faqManagementQueryPort;

    public FaqManagementCategoryListQueryService(FaqManagementQueryPort faqManagementQueryPort) {
        this.faqManagementQueryPort = faqManagementQueryPort;
    }

    @Override
    public List<FaqCategoryManagementResult> getCategories() {
        return faqManagementQueryPort.findAllCategories();
    }
}
