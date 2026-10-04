package com.tastyhouse.application.shop.port.in;

public interface ShopRequestCommandUseCase {

    void cancelRequest(ShopRequestCancelCommand command);

    Long addComment(ShopRequestCommentOwnerCreateCommand command);
}
