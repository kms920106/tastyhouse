package com.tastyhouse.application.menureview.port.in;

import com.tastyhouse.application.menureview.port.out.MenuReviewListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface MenuReviewProductListQueryUseCase {

    PageResult<MenuReviewListItemResult> findByProductId(Long productId, int page, int size);
}
