package com.tastyhouse.application.faq.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.application.faq.port.in.FaqCategoryCreateCommand;
import com.tastyhouse.application.faq.port.in.FaqCategoryCreateUseCase;
import com.tastyhouse.application.faq.port.out.write.FaqCategorySavePort;

@Service
@Transactional
class FaqCategoryCreateService implements FaqCategoryCreateUseCase {

    private final FaqCategorySavePort faqCategorySavePort;

    public FaqCategoryCreateService(FaqCategorySavePort faqCategorySavePort) {
        this.faqCategorySavePort = faqCategorySavePort;
    }

    @Override
    public Long createCategory(FaqCategoryCreateCommand command) {
        FaqCategory faqCategory = FaqCategory.of(command.name(), command.sort(), command.visible());
        FaqCategory saved = faqCategorySavePort.save(faqCategory);
        return saved.getFaqCategoryId().value();
    }
}
