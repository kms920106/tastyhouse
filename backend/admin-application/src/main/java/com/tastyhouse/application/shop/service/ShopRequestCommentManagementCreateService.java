package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopRequestCommentManagementCreateCommand;
import com.tastyhouse.application.shop.port.in.ShopRequestCommentManagementCreateUseCase;

@Service
@Transactional
class ShopRequestCommentManagementCreateService implements ShopRequestCommentManagementCreateUseCase {

    private final ShopRequestCommentService shopRequestCommentService;

    public ShopRequestCommentManagementCreateService(ShopRequestCommentService shopRequestCommentService) {
        this.shopRequestCommentService = shopRequestCommentService;
    }

    @Override
    public Long addComment(ShopRequestCommentManagementCreateCommand command) {
        Long requestId = command.requestId();
        Long adminId = command.adminId();
        String content = command.content();

        return shopRequestCommentService.addCommentByAdmin(requestId, adminId, content);
    }
}
