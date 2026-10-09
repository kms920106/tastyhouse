package com.tastyhouse.infrastructure.mybatis.banner;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.vo.BannerId;
import com.tastyhouse.application.banner.port.out.write.BannerLoadPort;
import com.tastyhouse.application.banner.port.out.write.BannerSavePort;

@Repository
class BannerMyBatisPersistenceAdapter implements BannerLoadPort, BannerSavePort {

    private final BannerMyBatisMapper bannerMyBatisMapper;

    public BannerMyBatisPersistenceAdapter(BannerMyBatisMapper bannerMyBatisMapper) {
        this.bannerMyBatisMapper = bannerMyBatisMapper;
    }

    @Override
    public Optional<Banner> findActiveById(BannerId id) {
        return bannerMyBatisMapper.selectActiveById(id.value()).map(BannerRowMapper::toDomain);
    }

    @Override
    public Banner save(Banner banner) {
        LocalDateTime now = LocalDateTime.now();

        if (banner.getId() == null) {
            BannerWriteRow row = BannerRowMapper.toWriteRow(banner, now, now);
            bannerMyBatisMapper.insert(row);
            return BannerRowMapper.toDomain(row);
        }

        BannerWriteRow row = BannerRowMapper.toWriteRow(banner, banner.getCreatedAt(), now);
        if (bannerMyBatisMapper.update(row) == 0) {
            throw new IllegalStateException("존재하지 않는 배너입니다: " + banner.getId());
        }
        return BannerRowMapper.toDomain(row);
    }
}
