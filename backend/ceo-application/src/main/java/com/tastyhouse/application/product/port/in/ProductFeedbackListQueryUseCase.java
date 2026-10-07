package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductFeedbackSummaryResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ProductFeedbackListQueryUseCase {

    PageResult<ProductFeedbackSummaryResult> getFeedbacks(Long ceoId, Long shopId, int page, int size);
}
