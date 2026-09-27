package com.tastyhouse.infrastructure.banner.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.banner.port.out.write.BannerState;
import com.tastyhouse.application.banner.port.out.write.BannerStatePort;

@Repository
public class BannerStatePortImpl implements BannerStatePort {
    private final BannerJpaRepository bannerJpaRepository;

    public BannerStatePortImpl(BannerJpaRepository bannerJpaRepository) {
        this.bannerJpaRepository = bannerJpaRepository;
    }

    @Override
    public Optional<BannerState> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return bannerJpaRepository.findByIdAndDeletedFalse(id)
            .map(BannerMapper::toState);
    }

    @Override
    public BannerState save(BannerState state) {
        if (state.id() == null) {
            BannerJpaEntity saved = bannerJpaRepository.save(BannerMapper.toEntity(state));
            return BannerMapper.toState(saved);
        }

        BannerJpaEntity entity = bannerJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배너입니다: " + state.id()));
        BannerMapper.applyChanges(entity, state);
        return BannerMapper.toState(entity);
    }
}
