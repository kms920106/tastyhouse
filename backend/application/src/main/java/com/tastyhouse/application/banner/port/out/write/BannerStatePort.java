package com.tastyhouse.application.banner.port.out.write;

import java.util.Optional;

public interface BannerStatePort {
    Optional<BannerState> findById(Long id);

    BannerState save(BannerState state);
}
