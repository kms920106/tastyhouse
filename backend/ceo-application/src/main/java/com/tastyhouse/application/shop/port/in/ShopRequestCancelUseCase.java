package com.tastyhouse.application.shop.port.in;

public interface ShopRequestCancelUseCase {

    void cancelRequest(ShopRequestCancelCommand command);
}
