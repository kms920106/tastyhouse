package com.tastyhouse.domain.banner.repository;

import java.util.Optional;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.vo.BannerId;

public interface BannerRepository {
    Optional<Banner> findById(BannerId id);

    Banner save(Banner banner);
}
