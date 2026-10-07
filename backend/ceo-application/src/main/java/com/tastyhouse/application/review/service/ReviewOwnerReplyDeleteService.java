package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewOwnerReplyDeleteCommand;
import com.tastyhouse.application.review.port.in.ReviewOwnerReplyDeleteUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ReviewOwnerReplyDeleteService implements ReviewOwnerReplyDeleteUseCase {

    private final ReviewOwnerReplyService reviewOwnerReplyService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ReviewOwnerReplyDeleteService(
        ReviewOwnerReplyService reviewOwnerReplyService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.reviewOwnerReplyService = reviewOwnerReplyService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void remove(ReviewOwnerReplyDeleteCommand command) {
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(command.ceoId(), shopId);
        reviewOwnerReplyService.remove(shopId, command.reviewId());
    }
}
