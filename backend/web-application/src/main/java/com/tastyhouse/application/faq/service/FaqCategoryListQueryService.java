package com.tastyhouse.application.faq.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.faq.port.in.FaqCategoryListQueryUseCase;
import com.tastyhouse.application.faq.port.out.FaqCategoryResult;
import com.tastyhouse.application.faq.port.out.FaqQueryPort;

@Service
@Transactional(readOnly = true)
class FaqCategoryListQueryService implements FaqCategoryListQueryUseCase {

    private final FaqQueryPort faqQueryPort;

    public FaqCategoryListQueryService(FaqQueryPort faqQueryPort) {
        this.faqQueryPort = faqQueryPort;
    }

    @Override
    public List<FaqCategoryResult> getFaqCategories() {
        return faqQueryPort.findVisibleCategories();
    }
}
