package com.tastyhouse.domain.faq.repository;

import java.util.Optional;

import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;

public interface FaqCategoryRepository {
    Optional<FaqCategory> findById(FaqCategoryId faqCategoryId);

    boolean existsActiveItemsByCategoryId(FaqCategoryId faqCategoryId);

    FaqCategory save(FaqCategory faqCategory);
}
