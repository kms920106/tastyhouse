package com.tastyhouse.application.faq.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.application.faq.port.in.FaqCategoryDeleteCommand;
import com.tastyhouse.application.faq.port.in.FaqCategoryDeleteUseCase;
import com.tastyhouse.application.faq.port.out.write.FaqCategoryLoadPort;
import com.tastyhouse.application.faq.port.out.write.FaqCategorySavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class FaqCategoryDeleteService implements FaqCategoryDeleteUseCase {

    private final FaqCategoryLoadPort faqCategoryLoadPort;
    private final FaqCategorySavePort faqCategorySavePort;
    private final FaqCategoryDeletionPolicy faqCategoryDeletionPolicy;

    public FaqCategoryDeleteService(FaqCategoryLoadPort faqCategoryLoadPort, FaqCategorySavePort faqCategorySavePort, FaqCategoryDeletionPolicy faqCategoryDeletionPolicy) {
        this.faqCategoryLoadPort = faqCategoryLoadPort;
        this.faqCategorySavePort = faqCategorySavePort;
        this.faqCategoryDeletionPolicy = faqCategoryDeletionPolicy;
    }

    @Override
    public void deleteCategory(FaqCategoryDeleteCommand command) {
        FaqCategoryId faqCategoryId = FaqCategoryId.of(command.faqCategoryId());
        FaqCategory faqCategory = findCategoryOrThrow(faqCategoryId);

        faqCategoryDeletionPolicy.delete(faqCategory);
        faqCategorySavePort.save(faqCategory);
    }

    private FaqCategory findCategoryOrThrow(FaqCategoryId faqCategoryId) {
        return faqCategoryLoadPort.findById(faqCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.FAQ_CATEGORY_NOT_FOUND));
    }
}
