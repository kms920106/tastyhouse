package com.tastyhouse.application.faq.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.application.faq.port.in.FaqCategoryUpdateCommand;
import com.tastyhouse.application.faq.port.in.FaqCategoryUpdateUseCase;
import com.tastyhouse.application.faq.port.out.write.FaqCategoryPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class FaqCategoryUpdateService implements FaqCategoryUpdateUseCase {

    private final FaqCategoryPersistencePort faqCategoryPersistencePort;

    public FaqCategoryUpdateService(FaqCategoryPersistencePort faqCategoryPersistencePort) {
        this.faqCategoryPersistencePort = faqCategoryPersistencePort;
    }

    @Override
    public void updateCategory(FaqCategoryUpdateCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        FaqCategory faqCategory = findCategoryOrThrow(faqCategoryId);

        faqCategory.update(command.name(), command.sort(), command.visible());
        faqCategoryPersistencePort.save(faqCategory);
    }

    private FaqCategory findCategoryOrThrow(FaqCategoryId faqCategoryId) {
        return faqCategoryPersistencePort.findById(faqCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
