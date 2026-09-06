package com.tastyhouse.application.banner.port.in;

import com.tastyhouse.application.banner.port.out.BannerListItemResult;
import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.domain.shared.page.PageResult;

@WebApp
public interface BannerQueryUseCase {

    PageResult<BannerListItemResult> getHomeBanners(int page, int size);

    PageResult<BannerListItemResult> getSidebarBanners(int page, int size);
}
