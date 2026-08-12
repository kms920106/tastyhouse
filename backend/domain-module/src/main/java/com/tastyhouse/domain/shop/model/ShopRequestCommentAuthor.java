package com.tastyhouse.domain.shop.model;

/**
 * 요청건 문의 스레드에 글을 쓴 주체.
 *
 * <p>도메인은 인증을 모르므로 이 값을 메서드 파라미터로 명시 전달받는다({@code ShopChangeActor}와 동일한
 * 패턴). SecurityContext·ThreadLocal에서 꺼내면 domain-module의 프레임워크-프리 원칙이 깨지고, 전달 누락을
 * 컴파일러가 잡아 주지 못한다.
 */
public record ShopRequestCommentAuthor(ShopRequestCommentAuthorType authorType, Long authorId) {

    public static ShopRequestCommentAuthor ceo(Long ceoId) {
        return new ShopRequestCommentAuthor(ShopRequestCommentAuthorType.CEO, ceoId);
    }

    public static ShopRequestCommentAuthor admin(Long adminId) {
        return new ShopRequestCommentAuthor(ShopRequestCommentAuthorType.ADMIN, adminId);
    }
}
