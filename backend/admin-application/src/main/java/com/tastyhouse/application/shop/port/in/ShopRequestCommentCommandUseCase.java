package com.tastyhouse.application.shop.port.in;

public interface ShopRequestCommentCommandUseCase {

    Long addComment(ShopRequestCommentManagementCreateCommand command);
}
