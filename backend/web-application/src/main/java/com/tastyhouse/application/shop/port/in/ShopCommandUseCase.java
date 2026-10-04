package com.tastyhouse.application.shop.port.in;

public interface ShopCommandUseCase {

    boolean toggleBookmark(ShopBookmarkToggleCommand command);
}
