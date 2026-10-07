package com.tastyhouse.application.faq.port.in;

import java.util.List;

import com.tastyhouse.application.faq.port.out.FaqCategoryResult;

public interface FaqCategoryListQueryUseCase {

    List<FaqCategoryResult> getFaqCategories();
}
