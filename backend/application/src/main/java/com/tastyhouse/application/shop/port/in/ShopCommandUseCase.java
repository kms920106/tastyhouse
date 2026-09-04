package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

/**
 * 회원용 가게 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ShopCommandService})을 알지 않는다.
 */
@WebApp
public interface ShopCommandUseCase {

    boolean toggleBookmark(ShopBookmarkToggleCommand command);
}
