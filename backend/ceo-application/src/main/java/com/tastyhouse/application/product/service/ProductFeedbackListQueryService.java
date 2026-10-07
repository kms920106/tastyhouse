package com.tastyhouse.application.product.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.application.product.port.in.ProductFeedbackListQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductFeedbackQueryPort;
import com.tastyhouse.application.product.port.out.ProductFeedbackSummaryResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional(readOnly = true)
class ProductFeedbackListQueryService implements ProductFeedbackListQueryUseCase {

    private final ProductFeedbackQueryPort productFeedbackQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductFeedbackListQueryService(
        ProductFeedbackQueryPort productFeedbackQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productFeedbackQueryPort = productFeedbackQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public PageResult<ProductFeedbackSummaryResult> getFeedbacks(Long ceoId, Long shopId, int page, int size) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        LocalDateTime since = LocalDateTime.now().minusDays(ProductFeedbackService.FEEDBACK_WINDOW_DAYS);
        return productFeedbackQueryPort.findFeedbackSummaries(
            shopId, since, ProductFeedbackType.ETC.name(), PageQuery.of(page, size)
        );
    }
}
