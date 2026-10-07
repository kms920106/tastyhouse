package com.tastyhouse.application.faq.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.application.faq.port.in.FaqCategoryDeleteCommand;
import com.tastyhouse.application.faq.port.in.FaqCategoryDeleteUseCase;
import com.tastyhouse.application.faq.port.out.write.FaqCategoryPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class FaqCategoryDeleteService implements FaqCategoryDeleteUseCase {

    private final FaqCategoryPersistencePort faqCategoryPersistencePort;
    private final FaqCategoryDeletionPolicy faqCategoryDeletionPolicy;

    public FaqCategoryDeleteService(FaqCategoryPersistencePort faqCategoryPersistencePort, FaqCategoryDeletionPolicy faqCategoryDeletionPolicy) {
        this.faqCategoryPersistencePort = faqCategoryPersistencePort;
        this.faqCategoryDeletionPolicy = faqCategoryDeletionPolicy;
    }

    @Override
    public void deleteCategory(FaqCategoryDeleteCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        FaqCategory faqCategory = findCategoryOrThrow(faqCategoryId);

        faqCategoryDeletionPolicy.delete(faqCategory);
        faqCategoryPersistencePort.save(faqCategory);
    }

    private FaqCategory findCategoryOrThrow(FaqCategoryId faqCategoryId) {
        return faqCategoryPersistencePort.findById(faqCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
