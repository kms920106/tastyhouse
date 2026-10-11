package com.tastyhouse.application.faq.port.out.write;

import com.tastyhouse.domain.faq.model.FaqCategory;

public interface FaqCategorySavePort {

    FaqCategory save(FaqCategory faqCategory);
}
