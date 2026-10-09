package com.tastyhouse.infrastructure.jpa.shop.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopBannerImage;
import com.tastyhouse.application.shop.port.out.write.ShopBannerImageSavePort;

@Repository
class ShopBannerImagePersistenceAdapter implements ShopBannerImageSavePort {

    private final ShopBannerImageJpaRepository shopBannerImageJpaRepository;

    public ShopBannerImagePersistenceAdapter(ShopBannerImageJpaRepository shopBannerImageJpaRepository) {
        this.shopBannerImageJpaRepository = shopBannerImageJpaRepository;
    }

    @Override
    public ShopBannerImage saveBannerImage(ShopBannerImage bannerImage) {
        if (bannerImage.getId() == null) {
            ShopBannerImageJpaEntity saved = shopBannerImageJpaRepository.save(ShopBannerImageMapper.toEntity(bannerImage));
            return ShopBannerImageMapper.toDomain(saved);
        }

        ShopBannerImageJpaEntity entity = shopBannerImageJpaRepository.findById(bannerImage.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배너 이미지입니다: " + bannerImage.getId()));
        return ShopBannerImageMapper.toDomain(entity);
    }

    @Override
    public void deleteBannerImageById(Long id) {
        shopBannerImageJpaRepository.deleteById(id);
    }
}
