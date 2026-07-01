package com.tastyhouse.core.domain.faq.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.faq.application.dto.FaqCategoryResult;
import com.tastyhouse.core.domain.faq.application.dto.FaqResult;

public interface FaqRepository {

    List<FaqCategoryResult> findAllActiveCategories();

    List<FaqResult> findAllActiveItems();

    List<FaqResult> findActiveItemsByCategoryId(Long categoryId);
}
