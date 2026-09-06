package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface ShopCommandUseCase {

    boolean toggleBookmark(ShopBookmarkToggleCommand command);
}
