package com.tastyhouse.application.menureview.port.in;

import java.util.List;

import com.tastyhouse.application.menureview.port.out.MenuReviewWritableItemResult;

public interface MenuReviewWritableItemQueryUseCase {

    List<MenuReviewWritableItemResult> findWritableItems(Long orderId, Long memberId);
}
