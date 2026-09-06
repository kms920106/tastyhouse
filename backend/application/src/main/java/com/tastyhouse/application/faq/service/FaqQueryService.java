package com.tastyhouse.application.faq.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.faq.port.out.FaqCategoryResult;
import com.tastyhouse.application.faq.port.out.FaqQueryPort;
import com.tastyhouse.application.faq.port.out.FaqResult;
import com.tastyhouse.application.faq.port.in.FaqQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class FaqQueryService implements FaqQueryUseCase {

    private final FaqQueryPort faqQueryPort;

    public FaqQueryService(FaqQueryPort faqQueryPort) {
        this.faqQueryPort = faqQueryPort;
    }

    @Override
    public List<FaqCategoryResult> getFaqCategories() {
        return faqQueryPort.findVisibleCategories();
    }

    @Override
    public List<FaqResult> getFaqList(Long categoryId) {
        return faqQueryPort.findVisibleFaqs(categoryId);
    }
}
