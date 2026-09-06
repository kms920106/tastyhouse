package com.tastyhouse.application.menureview.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface MenuReviewCommandUseCase {

    Long createMenuReview(MenuReviewCreateCommand command);

    void updateMenuReview(MenuReviewUpdateCommand command);

    void deleteMenuReview(MenuReviewDeleteCommand command);
}
