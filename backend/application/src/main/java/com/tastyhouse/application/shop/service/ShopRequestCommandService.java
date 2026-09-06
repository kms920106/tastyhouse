package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.service.ShopRequestCancelService;
import com.tastyhouse.domain.shop.service.ShopRequestCommentService;
import com.tastyhouse.application.shop.port.in.ShopRequestCancelCommand;
import com.tastyhouse.application.shop.port.in.ShopRequestCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopRequestCommentOwnerCreateCommand;

@Service
@CeoApp
@Transactional
public class ShopRequestCommandService implements ShopRequestCommandUseCase {

    private final ShopRequestCancelService shopRequestCancelService;
    private final ShopRequestCommentService shopRequestCommentService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRequestCommandService(
        ShopRequestCancelService shopRequestCancelService,
        ShopRequestCommentService shopRequestCommentService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRequestCancelService = shopRequestCancelService;
        this.shopRequestCommentService = shopRequestCommentService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void cancelRequest(ShopRequestCancelCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long requestId = command.requestId();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopRequestCancelService.cancel(requestId, shopId);
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
