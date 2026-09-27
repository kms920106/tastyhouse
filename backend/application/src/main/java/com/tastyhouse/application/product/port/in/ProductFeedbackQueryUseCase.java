package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductFeedbackSummaryResult;
import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@CeoApp
public interface ProductFeedbackQueryUseCase {

    PageResult<ProductFeedbackSummaryResult> getFeedbacks(Long ceoId, Long shopId, int page, int size);

    boolean getUnread(Long ceoId, Long shopId);
}
