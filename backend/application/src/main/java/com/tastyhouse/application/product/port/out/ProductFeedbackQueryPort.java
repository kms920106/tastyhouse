package com.tastyhouse.application.product.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface ProductFeedbackQueryPort {

    PageResult<ProductFeedbackSummaryResult> findFeedbackSummaries(Long shopId, LocalDateTime since, PageQuery pageQuery);
}
