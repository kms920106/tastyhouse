package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shop.port.in.ShopRequestCommentCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopRequestCommentManagementCreateCommand;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.service.ShopRequestCommentService;

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
