package com.tastyhouse.application.banner.port.in;

public interface BannerCommandUseCase {

    Long createBanner(BannerCreateCommand command);

    void updateBanner(BannerUpdateCommand command);

    void deleteBanner(BannerDeleteCommand command);
}
