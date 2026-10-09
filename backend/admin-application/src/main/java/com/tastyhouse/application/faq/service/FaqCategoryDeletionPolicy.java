package com.tastyhouse.application.faq.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.application.faq.port.out.write.FaqCategoryLoadPort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

@Service
public class FaqCategoryDeletionPolicy {

    private final FaqCategoryLoadPort faqCategoryLoadPort;

    public FaqCategoryDeletionPolicy(FaqCategoryLoadPort faqCategoryLoadPort) {
        this.faqCategoryLoadPort = faqCategoryLoadPort;
    }

    public void delete(FaqCategory faqCategory) {
        if (faqCategoryLoadPort.existsActiveItemsByCategoryId(faqCategory.getFaqCategoryId())) {
            throw new ApplicationException(AdminErrorCode.FAQ_CATEGORY_HAS_ITEMS);
        }

        faqCategory.delete();
    }
}
