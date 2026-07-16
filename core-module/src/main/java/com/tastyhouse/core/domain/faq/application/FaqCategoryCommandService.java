package com.tastyhouse.core.domain.faq.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.faq.domain.model.FaqCategory;
import com.tastyhouse.core.domain.faq.domain.repository.FaqCategoryRepository;
import com.tastyhouse.core.domain.faq.domain.vo.FaqCategoryId;
import com.tastyhouse.core.domain.faq.application.dto.command.FaqCategoryCreateCommand;
import com.tastyhouse.core.domain.faq.application.dto.command.FaqCategoryUpdateCommand;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class FaqCategoryCommandService {

    private final FaqCategoryRepository faqCategoryRepository;

    public FaqCategoryId createCategory(FaqCategoryCreateCommand command) {
        FaqCategory faqCategory = FaqCategory.of(command.name(), command.sort(), command.visible());
        FaqCategory saved = faqCategoryRepository.save(faqCategory);
        return saved.getFaqCategoryId();
    }

    public void updateCategory(FaqCategoryId faqCategoryId, FaqCategoryUpdateCommand command) {
        FaqCategory faqCategory = faqCategoryRepository.findById(faqCategoryId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FAQ_CATEGORY_NOT_FOUND));

        faqCategory.update(command.name(), command.sort(), command.visible());
    }

    public void deleteCategory(FaqCategoryId faqCategoryId) {
        FaqCategory faqCategory = faqCategoryRepository.findById(faqCategoryId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.FAQ_CATEGORY_NOT_FOUND));

        if (faqCategoryRepository.existsActiveItemsByCategoryId(faqCategoryId)) {
            throw new BusinessException(ErrorCode.FAQ_CATEGORY_HAS_ITEMS);
        }

        faqCategory.delete();
    }
}
