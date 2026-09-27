package com.tastyhouse.application.shop.port.out;

public record ShopRequestImageChangeDetailResult(
    String imageType,
    String imageTypeDescription,
    String imageUrl,
    String status,
    String rejectReason
) {

    public ShopRequestImageChangeDetailResult withImageTypeDescription(String imageTypeDescription) {
        return new ShopRequestImageChangeDetailResult(
            this.imageType,
            imageTypeDescription,
            this.imageUrl,
            this.status,
            this.rejectReason
        );
    }
}
