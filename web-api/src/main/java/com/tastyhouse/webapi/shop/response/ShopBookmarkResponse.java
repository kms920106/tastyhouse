package com.tastyhouse.webapi.shop.response;

public record ShopBookmarkResponse(
    boolean bookmarked
) {
    public static ShopBookmarkResponse from(boolean bookmarked) {
        return new ShopBookmarkResponse(bookmarked);
    }
}
