package com.tastyhouse.application.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.review.port.in.ReviewOwnerReplyUpdateCommand;
import com.tastyhouse.application.review.port.in.ReviewOwnerReplyUpdateUseCase;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ReviewOwnerReplyUpdateService implements ReviewOwnerReplyUpdateUseCase {

    private final ReviewOwnerReplyService reviewOwnerReplyService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ReviewOwnerReplyUpdateService(
        ReviewOwnerReplyService reviewOwnerReplyService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.reviewOwnerReplyService = reviewOwnerReplyService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void modify(ReviewOwnerReplyUpdateCommand command) {
        Long shopId = command.shopId();

        shopOwnershipValidator.validateOwnership(command.ceoId(), shopId);
        reviewOwnerReplyService.modify(shopId, command.reviewId(), command.content());
    }
}
