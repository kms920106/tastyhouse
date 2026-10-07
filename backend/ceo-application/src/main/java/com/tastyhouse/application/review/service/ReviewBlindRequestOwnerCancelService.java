package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewBlindRequestCancelCommand;
import com.tastyhouse.application.review.port.in.ReviewBlindRequestOwnerCancelUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ReviewBlindRequestOwnerCancelService implements ReviewBlindRequestOwnerCancelUseCase {

    private final ReviewBlindRequestService reviewBlindRequestService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ReviewBlindRequestOwnerCancelService(
        ReviewBlindRequestService reviewBlindRequestService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.reviewBlindRequestService = reviewBlindRequestService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void cancel(ReviewBlindRequestCancelCommand command) {
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(command.ceoId(), shopId);
        reviewBlindRequestService.cancel(command.blindRequestId(), shopId);
    }
}
