package com.tastyhouse.application.menureview.port.in;

import java.util.List;

import com.tastyhouse.application.menureview.port.out.MenuReviewListItemResult;
import com.tastyhouse.application.menureview.port.out.MenuReviewWritableItemResult;
import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@WebApp
public interface MenuReviewQueryUseCase {

    List<MenuReviewWritableItemResult> findWritableItems(Long orderId, Long memberId);

    PageResult<MenuReviewListItemResult> findByProductId(Long productId, int page, int size);
}
