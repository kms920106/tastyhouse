package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.List;

import com.tastyhouse.application.faq.port.out.FaqCategoryManagementResult;
import com.tastyhouse.application.faq.port.out.FaqDetailResult;
import com.tastyhouse.application.faq.port.out.FaqManagementListItemResult;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface FaqManagementQueryUseCase {

    List<FaqCategoryManagementResult> getCategories();

    FaqCategoryManagementResult getCategory(Long categoryId);

    PageResult<FaqManagementListItemResult> getFaqs(Long categoryId, String question, Boolean visible, int page, int size);

    FaqDetailResult getFaq(Long id);
}
