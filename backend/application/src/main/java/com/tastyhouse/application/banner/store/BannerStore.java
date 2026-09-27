package com.tastyhouse.application.banner.store;

import java.util.Optional;

import com.tastyhouse.application.banner.port.out.write.BannerStatePort;
import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.vo.BannerId;

public class BannerStore implements BannerRepository {
    private final BannerStatePort bannerStatePort;

    public BannerStore(BannerStatePort bannerStatePort) {
        this.bannerStatePort = bannerStatePort;
    }

    @Override
    public Optional<Banner> findById(BannerId id) {
        if (id == null) {
            return Optional.empty();
        }
        return bannerStatePort.findById(id.value()).map(BannerStateMapper::toDomain);
    }

    @Override
    public Banner save(Banner banner) {
        return BannerStateMapper.toDomain(bannerStatePort.save(BannerStateMapper.toState(banner)));
    }
}
