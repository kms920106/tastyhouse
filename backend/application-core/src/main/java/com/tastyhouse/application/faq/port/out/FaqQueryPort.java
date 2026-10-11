package com.tastyhouse.application.faq.port.out;

import java.util.List;

public interface FaqQueryPort {

    List<FaqCategoryResult> findVisibleCategories();

    List<FaqResult> findVisibleFaqs(Long categoryId);
}
