package com.tastyhouse.application.product.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductFeedbackReadCommand;
import com.tastyhouse.application.product.port.in.ProductFeedbackReadUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductFeedbackReadService implements ProductFeedbackReadUseCase {

    private final ProductFeedbackService productFeedbackService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductFeedbackReadService(
        ProductFeedbackService productFeedbackService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productFeedbackService = productFeedbackService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void markRead(ProductFeedbackReadCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        productFeedbackService.markRead(ShopId.of(shopId), LocalDateTime.now());
    }
}
