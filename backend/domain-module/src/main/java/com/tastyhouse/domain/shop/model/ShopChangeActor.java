package com.tastyhouse.domain.shop.model;

/**
 * 가게 설정을 변경한 주체.
 *
 * <p>도메인 서비스는 인증을 모르므로 이 값을 메서드 파라미터로 명시 전달받는다. ThreadLocal이나
 * SecurityContext에서 꺼내면 domain-module의 프레임워크-프리 원칙이 깨지고, 전달 누락을 컴파일러가
 * 잡아 주지 못한다.
 */
public record ShopChangeActor(ShopChangeActorType actorType, Long actorId) {

    public static ShopChangeActor ceo(Long ceoId) {
        return new ShopChangeActor(ShopChangeActorType.CEO, ceoId);
    }

    public static ShopChangeActor admin(Long adminId) {
        return new ShopChangeActor(ShopChangeActorType.ADMIN, adminId);
    }
}
