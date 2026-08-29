package com.tastyhouse.webapi.shop.application.port.in;

/**
 * 회원용 가게 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopCommandService})을 알지 않는다.
 */
public interface ShopCommandUseCase {

    boolean toggleBookmark(ShopBookmarkToggleCommand command);
}
