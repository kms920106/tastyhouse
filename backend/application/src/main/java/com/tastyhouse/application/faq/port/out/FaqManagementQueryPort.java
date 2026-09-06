package com.tastyhouse.application.faq.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface FaqManagementQueryPort {

    List<FaqCategoryManagementResult> findAllCategories();

    Optional<FaqCategoryManagementResult> findCategoryDetailById(Long categoryId);

    PageResult<FaqManagementListItemResult> findAllFaqs(FaqSearchCondition condition, PageQuery pageQuery);

    Optional<FaqDetailResult> findFaqDetailById(Long id);
}
