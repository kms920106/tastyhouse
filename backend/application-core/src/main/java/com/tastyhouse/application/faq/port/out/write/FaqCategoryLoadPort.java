package com.tastyhouse.application.faq.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;

public interface FaqCategoryLoadPort {

    Optional<FaqCategory> findActiveById(FaqCategoryId faqCategoryId);

    boolean existsActiveItemsByCategoryId(FaqCategoryId faqCategoryId);
}
