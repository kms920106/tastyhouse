package com.tastyhouse.application.menureview.port.out;

import java.util.List;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface MenuReviewQueryPort {

    List<MenuReviewWritableItemResult> findWritableItemsByOrderId(Long orderId);

    PageResult<MenuReviewListItemResult> findVisibleByProductId(Long productId, PageQuery pageQuery);
}
