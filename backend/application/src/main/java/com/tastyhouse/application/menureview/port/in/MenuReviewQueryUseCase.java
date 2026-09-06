package com.tastyhouse.application.menureview.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import com.tastyhouse.domain.shared.page.PageResult;

import com.tastyhouse.application.menureview.port.out.MenuReviewListItemResult;
import com.tastyhouse.application.menureview.port.out.MenuReviewWritableItemResult;

@WebApp
public interface MenuReviewQueryUseCase {

    List<MenuReviewWritableItemResult> findWritableItems(Long orderId, Long memberId);

    PageResult<MenuReviewListItemResult> findByProductId(Long productId, int page, int size);
}
