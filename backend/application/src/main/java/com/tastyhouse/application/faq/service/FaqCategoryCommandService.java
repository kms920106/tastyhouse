package com.tastyhouse.application.faq.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.faq.port.in.FaqCategoryCommandUseCase;
import com.tastyhouse.application.faq.port.in.FaqCategoryCreateCommand;
import com.tastyhouse.application.faq.port.in.FaqCategoryDeleteCommand;
import com.tastyhouse.application.faq.port.in.FaqCategoryUpdateCommand;
import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.domain.faq.repository.FaqCategoryRepository;
import com.tastyhouse.domain.faq.service.FaqCategoryDeletionPolicy;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Service
@AdminApp
@Transactional
public class FaqCategoryCommandService implements FaqCategoryCommandUseCase {

    private final FaqCategoryRepository faqCategoryRepository;
    private final FaqCategoryDeletionPolicy faqCategoryDeletionPolicy;

    public FaqCategoryCommandService(FaqCategoryRepository faqCategoryRepository, FaqCategoryDeletionPolicy faqCategoryDeletionPolicy) {
        this.faqCategoryRepository = faqCategoryRepository;
        this.faqCategoryDeletionPolicy = faqCategoryDeletionPolicy;
    }

    @Override
    public Long createCategory(FaqCategoryCreateCommand command) {
        FaqCategory faqCategory = FaqCategory.of(command.name(), command.sort(), command.visible());
        FaqCategory saved = faqCategoryRepository.save(faqCategory);
        return saved.getFaqCategoryId().value();
    }

    @Override
    public void updateCategory(FaqCategoryUpdateCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        FaqCategory faqCategory = findCategoryOrThrow(faqCategoryId);

        faqCategory.update(command.name(), command.sort(), command.visible());
        faqCategoryRepository.save(faqCategory);
    }

    @Override
    public void deleteCategory(FaqCategoryDeleteCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        FaqCategory faqCategory = findCategoryOrThrow(faqCategoryId);

        faqCategoryDeletionPolicy.delete(faqCategory);
        faqCategoryRepository.save(faqCategory);
    }

    private FaqCategory findCategoryOrThrow(FaqCategoryId faqCategoryId) {
        return faqCategoryRepository.findById(faqCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
