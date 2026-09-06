package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.product.port.out.ProductFeedbackSummaryResult;

@CeoApp
public interface ProductFeedbackQueryUseCase {

    PageResult<ProductFeedbackSummaryResult> getFeedbacks(Long ceoId, Long shopId, int page, int size);

    boolean getUnread(Long ceoId, Long shopId);
}
