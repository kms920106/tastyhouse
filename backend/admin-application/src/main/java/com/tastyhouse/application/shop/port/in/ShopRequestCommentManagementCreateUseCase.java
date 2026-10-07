package com.tastyhouse.application.shop.port.in;

public interface ShopRequestCommentManagementCreateUseCase {

    Long addComment(ShopRequestCommentManagementCreateCommand command);
}
