package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ShopRequestCommandUseCase {

    void cancelRequest(ShopRequestCancelCommand command);

    Long addComment(ShopRequestCommentOwnerCreateCommand command);
}
