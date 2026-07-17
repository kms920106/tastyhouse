package com.tastyhouse.core.domain.faq.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.faq.domain.model.FaqCategory;
import com.tastyhouse.core.domain.faq.domain.vo.FaqCategoryId;
import com.tastyhouse.core.domain.faq.application.dto.result.FaqCategoryManagementResult;
import com.tastyhouse.core.domain.faq.application.dto.result.FaqCategoryResult;

public interface FaqCategoryRepository {

    List<FaqCategoryResult> findAllActiveCategories();

    List<FaqCategoryManagementResult> findAllCategories();

    Optional<FaqCategory> findById(FaqCategoryId faqCategoryId);

    boolean existsActiveItemsByCategoryId(FaqCategoryId faqCategoryId);

    FaqCategory save(FaqCategory faqCategory);
}
