package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopBestListItemViewResult;

public interface ShopBestListQueryUseCase {

    PageResult<ShopBestListItemViewResult> searchBestShops(Long memberId, int page, int size);
}
