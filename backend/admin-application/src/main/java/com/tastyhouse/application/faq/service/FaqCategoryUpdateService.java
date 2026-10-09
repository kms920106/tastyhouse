package com.tastyhouse.application.faq.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.application.faq.port.in.FaqCategoryUpdateCommand;
import com.tastyhouse.application.faq.port.in.FaqCategoryUpdateUseCase;
import com.tastyhouse.application.faq.port.out.write.FaqCategoryLoadPort;
import com.tastyhouse.application.faq.port.out.write.FaqCategorySavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class FaqCategoryUpdateService implements FaqCategoryUpdateUseCase {

    private final FaqCategoryLoadPort faqCategoryLoadPort;
    private final FaqCategorySavePort faqCategorySavePort;

    public FaqCategoryUpdateService(FaqCategoryLoadPort faqCategoryLoadPort, FaqCategorySavePort faqCategorySavePort) {
        this.faqCategoryLoadPort = faqCategoryLoadPort;
        this.faqCategorySavePort = faqCategorySavePort;
    }

    @Override
    public void updateCategory(FaqCategoryUpdateCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        FaqCategory faqCategory = findCategoryOrThrow(faqCategoryId);

        faqCategory.update(command.name(), command.sort(), command.visible());
        faqCategorySavePort.save(faqCategory);
    }

    private FaqCategory findCategoryOrThrow(FaqCategoryId faqCategoryId) {
        return faqCategoryLoadPort.findById(faqCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
