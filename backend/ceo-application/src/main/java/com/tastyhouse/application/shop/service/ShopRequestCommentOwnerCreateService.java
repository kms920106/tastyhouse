package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopRequestCommentOwnerCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopRequestCommentOwnerCreateUseCase;

@Service
@Transactional
class ShopRequestCommentOwnerCreateService implements ShopRequestCommentOwnerCreateUseCase {

    private final ShopRequestCommentService shopRequestCommentService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRequestCommentOwnerCreateService(
        ShopRequestCommentService shopRequestCommentService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRequestCommentService = shopRequestCommentService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Long addComment(ShopRequestCommentOwnerCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long requestId = command.requestId();
        String content = command.content();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopRequestCommentService.addCommentByCeo(requestId, shopId, ceoId, content);
    }
}
