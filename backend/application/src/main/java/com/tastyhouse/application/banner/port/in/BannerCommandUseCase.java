package com.tastyhouse.application.banner.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface BannerCommandUseCase {

    Long createBanner(BannerCreateCommand command);

    void updateBanner(BannerUpdateCommand command);

    void deleteBanner(BannerDeleteCommand command);
}
