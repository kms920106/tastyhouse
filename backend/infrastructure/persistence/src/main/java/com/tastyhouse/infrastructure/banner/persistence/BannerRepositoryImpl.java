package com.tastyhouse.infrastructure.banner.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.banner.model.Banner;
import com.tastyhouse.domain.banner.repository.BannerRepository;
import com.tastyhouse.domain.banner.vo.BannerId;

@Repository
public class BannerRepositoryImpl implements BannerRepository {
    private final BannerJpaRepository bannerJpaRepository;

    public BannerRepositoryImpl(BannerJpaRepository bannerJpaRepository) {
        this.bannerJpaRepository = bannerJpaRepository;
    }

    @Override
    public Optional<Banner> findById(BannerId id) {
        if (id == null) {
            return Optional.empty();
        }
        return bannerJpaRepository.findByIdAndDeletedFalse(id.value())
            .map(BannerMapper::toDomain);
    }

    @Override
    public Banner save(Banner banner) {
        if (banner.getId() == null) {
            BannerJpaEntity saved = bannerJpaRepository.save(BannerMapper.toEntity(banner));
            return BannerMapper.toDomain(saved);
        }

        BannerJpaEntity entity = bannerJpaRepository.findById(banner.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배너입니다: " + banner.getId()));
        BannerMapper.applyChanges(entity, banner);
        return BannerMapper.toDomain(entity);
    }
}
