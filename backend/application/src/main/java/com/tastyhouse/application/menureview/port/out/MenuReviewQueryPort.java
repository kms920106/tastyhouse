package com.tastyhouse.application.menureview.port.out;

import java.util.List;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface MenuReviewQueryPort {

    List<MenuReviewWritableItemResult> findWritableItemsByOrderId(Long orderId);

    PageResult<MenuReviewListItemResult> findVisibleByProductId(Long productId, PageQuery pageQuery);
}
