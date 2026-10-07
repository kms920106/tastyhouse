package com.tastyhouse.application.banner.port.in;

import com.tastyhouse.application.banner.port.out.BannerListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface SidebarBannerListQueryUseCase {

    PageResult<BannerListItemResult> getSidebarBanners(int page, int size);
}
