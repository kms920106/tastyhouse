package com.tastyhouse.domain.shop.model;

public record ShopChangeActor(ShopChangeActorType actorType, Long actorId) {
    public static ShopChangeActor ceo(Long ceoId) {
        return new ShopChangeActor(ShopChangeActorType.CEO, ceoId);
    }

    public static ShopChangeActor admin(Long adminId) {
        return new ShopChangeActor(ShopChangeActorType.ADMIN, adminId);
    }
}
