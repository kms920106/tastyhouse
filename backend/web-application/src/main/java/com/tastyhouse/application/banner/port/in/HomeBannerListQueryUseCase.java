package com.tastyhouse.application.banner.port.in;

import com.tastyhouse.application.banner.port.out.BannerListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface HomeBannerListQueryUseCase {

    PageResult<BannerListItemResult> getHomeBanners(int page, int size);
}
