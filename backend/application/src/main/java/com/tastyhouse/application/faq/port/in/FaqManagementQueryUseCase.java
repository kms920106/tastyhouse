package com.tastyhouse.application.faq.port.in;

import java.util.List;

import com.tastyhouse.application.faq.port.out.FaqCategoryManagementResult;
import com.tastyhouse.application.faq.port.out.FaqDetailResult;
import com.tastyhouse.application.faq.port.out.FaqManagementListItemResult;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@AdminApp
public interface FaqManagementQueryUseCase {

    List<FaqCategoryManagementResult> getCategories();

    FaqCategoryManagementResult getCategory(Long categoryId);

    PageResult<FaqManagementListItemResult> getFaqs(Long categoryId, String question, Boolean visible, int page, int size);

    FaqDetailResult getFaq(Long id);
}
