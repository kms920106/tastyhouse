package com.tastyhouse.application.shop.port.out.write;

public record ShopNoticeImageState(
    Long id,
    Long shopNoticeId,
    Long imageFileId,
    int sortOrder
) {
}
