package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shop.port.in.ShopRequestCommentCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopRequestCommentManagementCreateCommand;

@Service
@AdminApp
@Transactional
public class ShopRequestCommentCommandService implements ShopRequestCommentCommandUseCase {

    private final ShopRequestCommentService shopRequestCommentService;

    public ShopRequestCommentCommandService(ShopRequestCommentService shopRequestCommentService) {
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
