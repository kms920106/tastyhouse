package com.tastyhouse.application.menureview.port.in;

public interface MenuReviewCommandUseCase {

    Long createMenuReview(MenuReviewCreateCommand command);

    void updateMenuReview(MenuReviewUpdateCommand command);

    void deleteMenuReview(MenuReviewDeleteCommand command);
}
