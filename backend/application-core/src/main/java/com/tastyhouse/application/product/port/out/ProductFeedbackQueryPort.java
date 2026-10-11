package com.tastyhouse.application.product.port.out;

import java.time.LocalDateTime;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ProductFeedbackQueryPort {

    PageResult<ProductFeedbackSummaryResult> findFeedbackSummaries(
        Long shopId,
        LocalDateTime since,
        String contentRequiredType,
        PageQuery pageQuery
    );
}
