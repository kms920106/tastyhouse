package com.tastyhouse.application.faq.port.in;

import com.tastyhouse.application.faq.port.out.FaqManagementListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface FaqManagementListQueryUseCase {

    PageResult<FaqManagementListItemResult> getFaqs(Long categoryId, String question, Boolean visible, int page, int size);
}
