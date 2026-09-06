package com.tastyhouse.domain.faq.service;

import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.domain.faq.repository.FaqCategoryRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public class FaqCategoryDeletionPolicy {
    private final FaqCategoryRepository faqCategoryRepository;

    public FaqCategoryDeletionPolicy(FaqCategoryRepository faqCategoryRepository) {
        this.faqCategoryRepository = faqCategoryRepository;
    }

    public void delete(FaqCategory faqCategory) {
        if (faqCategoryRepository.existsActiveItemsByCategoryId(faqCategory.getFaqCategoryId())) {
            throw new BusinessException(ErrorCode.FAQ_CATEGORY_HAS_ITEMS);
        }

        faqCategory.delete();
    }
}
