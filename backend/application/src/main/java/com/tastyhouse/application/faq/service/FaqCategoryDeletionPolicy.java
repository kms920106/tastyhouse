package com.tastyhouse.application.faq.service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.application.faq.port.out.write.FaqCategoryPersistencePort;
import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
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
