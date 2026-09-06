package com.tastyhouse.domain.shop.model;

public record ShopRequestCommentAuthor(ShopRequestCommentAuthorType authorType, Long authorId) {
    public static ShopRequestCommentAuthor ceo(Long ceoId) {
        return new ShopRequestCommentAuthor(ShopRequestCommentAuthorType.CEO, ceoId);
    }

    public static ShopRequestCommentAuthor admin(Long adminId) {
        return new ShopRequestCommentAuthor(ShopRequestCommentAuthorType.ADMIN, adminId);
    }
}
