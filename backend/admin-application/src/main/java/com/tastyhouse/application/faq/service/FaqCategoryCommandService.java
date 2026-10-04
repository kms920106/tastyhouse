package com.tastyhouse.application.faq.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.application.faq.port.in.FaqCategoryCommandUseCase;
import com.tastyhouse.application.faq.port.in.FaqCategoryCreateCommand;
import com.tastyhouse.application.faq.port.in.FaqCategoryDeleteCommand;
import com.tastyhouse.application.faq.port.in.FaqCategoryUpdateCommand;
import com.tastyhouse.application.faq.port.out.write.FaqCategoryPersistencePort;

@Service
@Transactional
public class FaqCategoryCommandService implements FaqCategoryCommandUseCase {

    private final FaqCategoryPersistencePort faqCategoryPersistencePort;
    private final FaqCategoryDeletionPolicy faqCategoryDeletionPolicy;

    public FaqCategoryCommandService(FaqCategoryPersistencePort faqCategoryPersistencePort, FaqCategoryDeletionPolicy faqCategoryDeletionPolicy) {
        this.faqCategoryPersistencePort = faqCategoryPersistencePort;
        this.faqCategoryDeletionPolicy = faqCategoryDeletionPolicy;
    }

    @Override
    public Long createCategory(FaqCategoryCreateCommand command) {
        FaqCategory faqCategory = FaqCategory.of(command.name(), command.sort(), command.visible());
        FaqCategory saved = faqCategoryPersistencePort.save(faqCategory);
        return saved.getFaqCategoryId().value();
    }

    @Override
    public void updateCategory(FaqCategoryUpdateCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        FaqCategory faqCategory = findCategoryOrThrow(faqCategoryId);

        faqCategory.update(command.name(), command.sort(), command.visible());
        faqCategoryPersistencePort.save(faqCategory);
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
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
