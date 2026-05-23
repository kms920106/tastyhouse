package com.tastyhouse.core.domain.faq.domain.repository;

import com.tastyhouse.core.domain.faq.application.dto.FaqCategoryResult;
import com.tastyhouse.core.domain.faq.application.dto.FaqResult;

import java.util.List;

public interface FaqRepository {

    List<FaqCategoryResult> findAllActiveCategories();

    List<FaqResult> findAllActiveItems();

    List<FaqResult> findActiveItemsByCategoryId(Long categoryId);
}
