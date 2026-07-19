package com.tastyhouse.core.domain.shop.application.dto.command;

public record ShopChoiceSaveCommand(
    String title,
    String content
) {

    public static ShopChoiceSaveCommand of(String title, String content) {
        return new ShopChoiceSaveCommand(title, content);
    }
}
