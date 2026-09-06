package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductFeedbackOwnerCommandUseCase;
import com.tastyhouse.application.product.port.in.ProductFeedbackReadCommand;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.product.service.ProductFeedbackService;
import com.tastyhouse.domain.shop.vo.ShopId;

@Service
@CeoApp
@Transactional
public class ProductFeedbackOwnerCommandService implements ProductFeedbackOwnerCommandUseCase {

    private final ProductFeedbackService productFeedbackService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductFeedbackOwnerCommandService(
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
