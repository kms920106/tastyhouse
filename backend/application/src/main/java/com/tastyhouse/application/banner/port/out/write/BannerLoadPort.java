package com.tastyhouse.application.banner.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.vo.BannerId;

public interface BannerLoadPort {

    Optional<Banner> findById(BannerId id);
}
