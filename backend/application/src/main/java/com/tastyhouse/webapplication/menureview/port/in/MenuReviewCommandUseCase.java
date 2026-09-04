package com.tastyhouse.webapplication.menureview.port.in;

/**
 * 메뉴 평가 쓰기 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code MenuReviewCommandService})을 알지 않는다.
 */
public interface MenuReviewCommandUseCase {

    Long createMenuReview(MenuReviewCreateCommand command);

    void updateMenuReview(MenuReviewUpdateCommand command);

    void deleteMenuReview(MenuReviewDeleteCommand command);
}
