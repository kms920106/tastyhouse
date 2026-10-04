package com.tastyhouse.application.faq.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.application.faq.port.out.write.FaqCategoryPersistencePort;

@Service
public class FaqCategoryDeletionPolicy {

    private final FaqCategoryPersistencePort faqCategoryPersistencePort;

    public FaqCategoryDeletionPolicy(FaqCategoryPersistencePort faqCategoryPersistencePort) {
        this.faqCategoryPersistencePort = faqCategoryPersistencePort;
    }

    public void delete(FaqCategory faqCategory) {
        if (faqCategoryPersistencePort.existsActiveItemsByCategoryId(faqCategory.getFaqCategoryId())) {
            throw new BusinessException(ErrorCode.FAQ_CATEGORY_HAS_ITEMS);
        }

        faqCategory.delete();
    }
}
