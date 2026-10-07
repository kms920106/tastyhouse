package com.tastyhouse.application.shop.port.in;

public interface ShopBookmarkStatusQueryUseCase {

    boolean isBookmarked(Long shopId, Long memberId);
}
