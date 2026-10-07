package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.application.faq.port.out.FaqCategoryManagementResult;

public interface FaqManagementCategoryDetailQueryUseCase {

    FaqCategoryManagementResult getCategory(Long categoryId);
}
