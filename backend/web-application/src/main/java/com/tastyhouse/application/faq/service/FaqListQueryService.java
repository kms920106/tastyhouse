package com.tastyhouse.application.faq.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.faq.port.in.FaqListQueryUseCase;
import com.tastyhouse.application.faq.port.out.FaqQueryPort;
import com.tastyhouse.application.faq.port.out.FaqResult;

@Service
@Transactional(readOnly = true)
class FaqListQueryService implements FaqListQueryUseCase {

    private final FaqQueryPort faqQueryPort;

    public FaqListQueryService(FaqQueryPort faqQueryPort) {
        this.faqQueryPort = faqQueryPort;
    }

    @Override
    public List<FaqResult> getFaqList(Long categoryId) {
        return faqQueryPort.findVisibleFaqs(categoryId);
    }
}
