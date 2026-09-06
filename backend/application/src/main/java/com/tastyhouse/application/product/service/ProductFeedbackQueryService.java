package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductFeedbackQueryUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.product.service.ProductFeedbackService;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.ProductFeedbackQueryPort;
import com.tastyhouse.application.product.port.out.ProductFeedbackSummaryResult;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ProductFeedbackQueryService implements ProductFeedbackQueryUseCase {

    private final ProductFeedbackQueryPort productFeedbackQueryPort;
    private final ProductFeedbackService productFeedbackService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductFeedbackQueryService(
        ProductFeedbackQueryPort productFeedbackQueryPort,
        ProductFeedbackService productFeedbackService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productFeedbackQueryPort = productFeedbackQueryPort;
        this.productFeedbackService = productFeedbackService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public PageResult<ProductFeedbackSummaryResult> getFeedbacks(Long ceoId, Long shopId, int page, int size) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        LocalDateTime since = LocalDateTime.now().minusDays(ProductFeedbackService.FEEDBACK_WINDOW_DAYS);
        return productFeedbackQueryPort.findFeedbackSummaries(
            shopId, since, PageQuery.of(page, size)
        );
    }

    @Override
    public boolean getUnread(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return productFeedbackService.hasUnread(ShopId.of(shopId), LocalDateTime.now());
    }

}
