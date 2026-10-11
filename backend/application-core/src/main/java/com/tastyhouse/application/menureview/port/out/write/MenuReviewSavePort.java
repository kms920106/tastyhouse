package com.tastyhouse.application.menureview.port.out.write;

import com.tastyhouse.domain.menureview.model.MenuReview;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;

public interface MenuReviewSavePort {

    MenuReview save(MenuReview menuReview);

    void deleteById(MenuReviewId menuReviewId);
}
