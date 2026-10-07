package com.tastyhouse.application.faq.port.in;

import java.util.List;

import com.tastyhouse.application.faq.port.out.FaqCategoryManagementResult;

public interface FaqManagementCategoryListQueryUseCase {

    List<FaqCategoryManagementResult> getCategories();
}
