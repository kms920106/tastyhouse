package com.tastyhouse.application.banner.port.out.write;

import com.tastyhouse.domain.banner.model.Banner;

public interface BannerSavePort {

    Banner save(Banner banner);
}
