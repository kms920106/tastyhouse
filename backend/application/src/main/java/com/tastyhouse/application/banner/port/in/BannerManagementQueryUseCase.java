package com.tastyhouse.application.banner.port.in;

import com.tastyhouse.application.banner.port.out.BannerDetailResult;
import com.tastyhouse.application.banner.port.out.BannerManagementListItemResult;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface BannerManagementQueryUseCase {

    PageResult<BannerManagementListItemResult> getBanners(String type, String title, Boolean visible, int page, int size);

    BannerDetailResult getBanner(Long id);
}
