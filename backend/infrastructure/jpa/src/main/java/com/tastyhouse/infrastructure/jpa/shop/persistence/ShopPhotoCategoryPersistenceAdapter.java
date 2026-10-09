package com.tastyhouse.infrastructure.jpa.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;
import com.tastyhouse.application.shop.port.out.write.ShopPhotoCategoryLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopPhotoCategorySavePort;

@Repository
class ShopPhotoCategoryPersistenceAdapter implements ShopPhotoCategoryLoadPort, ShopPhotoCategorySavePort {

    private final ShopPhotoCategoryJpaRepository shopPhotoCategoryJpaRepository;
    private final ShopPhotoCategoryImageJpaRepository shopPhotoCategoryImageJpaRepository;

    public ShopPhotoCategoryPersistenceAdapter(
        ShopPhotoCategoryJpaRepository shopPhotoCategoryJpaRepository,
        ShopPhotoCategoryImageJpaRepository shopPhotoCategoryImageJpaRepository
    ) {
        this.shopPhotoCategoryJpaRepository = shopPhotoCategoryJpaRepository;
        this.shopPhotoCategoryImageJpaRepository = shopPhotoCategoryImageJpaRepository;
    }

    @Override
    public Optional<ShopPhotoCategory> findPhotoCategoryById(Long id) {
        return shopPhotoCategoryJpaRepository.findById(id).map(ShopPhotoCategoryMapper::toDomain);
    }

    @Override
    public ShopPhotoCategory savePhotoCategory(ShopPhotoCategory photoCategory) {
        if (photoCategory.getId() == null) {
            ShopPhotoCategoryJpaEntity saved = shopPhotoCategoryJpaRepository.save(ShopPhotoCategoryMapper.toEntity(photoCategory));
            return ShopPhotoCategoryMapper.toDomain(saved);
        }

        ShopPhotoCategoryJpaEntity entity = shopPhotoCategoryJpaRepository.findById(photoCategory.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 사진 카테고리입니다: " + photoCategory.getId()));
        ShopPhotoCategoryMapper.applyChanges(entity, photoCategory);
        return ShopPhotoCategoryMapper.toDomain(entity);
    }

    @Override
    public void deletePhotoCategoryById(Long id) {
        shopPhotoCategoryJpaRepository.deleteById(id);
    }

    @Override
    public Optional<ShopPhotoCategoryImage> findPhotoCategoryImageById(Long id) {
        return shopPhotoCategoryImageJpaRepository.findById(id).map(ShopPhotoCategoryImageMapper::toDomain);
    }

    @Override
    public ShopPhotoCategoryImage savePhotoCategoryImage(ShopPhotoCategoryImage photoCategoryImage) {
        if (photoCategoryImage.getId() == null) {
            ShopPhotoCategoryImageJpaEntity saved = shopPhotoCategoryImageJpaRepository.save(ShopPhotoCategoryImageMapper.toEntity(photoCategoryImage));
            return ShopPhotoCategoryImageMapper.toDomain(saved);
        }

        ShopPhotoCategoryImageJpaEntity entity = shopPhotoCategoryImageJpaRepository.findById(photoCategoryImage.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 사진 카테고리 이미지입니다: " + photoCategoryImage.getId()));
        ShopPhotoCategoryImageMapper.applyChanges(entity, photoCategoryImage);
        return ShopPhotoCategoryImageMapper.toDomain(entity);
    }

    @Override
    public void deletePhotoCategoryImageById(Long id) {
        shopPhotoCategoryImageJpaRepository.deleteById(id);
    }
}
