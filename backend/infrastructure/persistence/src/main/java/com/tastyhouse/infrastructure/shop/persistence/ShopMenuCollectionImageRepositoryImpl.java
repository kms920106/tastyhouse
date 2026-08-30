package com.tastyhouse.infrastructure.shop.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.repository.ShopMenuCollectionImageRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;

/**
 * 메뉴모음컷 write 어댑터. 표현 목적 조회는 {@code ShopQueryDao}가 담당한다.
 */
@Repository
public class ShopMenuCollectionImageRepositoryImpl implements ShopMenuCollectionImageRepository {

    private final ShopMenuCollectionImageJpaRepository shopMenuCollectionImageJpaRepository;

    public ShopMenuCollectionImageRepositoryImpl(
        ShopMenuCollectionImageJpaRepository shopMenuCollectionImageJpaRepository
    ) {
        this.shopMenuCollectionImageJpaRepository = shopMenuCollectionImageJpaRepository;
    }

    @Override
    public ShopMenuCollectionImage save(ShopMenuCollectionImage image) {
        if (image.getId() == null) {
            ShopMenuCollectionImageJpaEntity saved =
                shopMenuCollectionImageJpaRepository.save(ShopMenuCollectionImageMapper.toEntity(image));
            return ShopMenuCollectionImageMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ShopMenuCollectionImageJpaEntity entity = shopMenuCollectionImageJpaRepository.findById(image.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메뉴모음컷입니다: " + image.getId()));
        ShopMenuCollectionImageMapper.applyChanges(entity, image);
        return ShopMenuCollectionImageMapper.toDomain(entity);
    }

    @Override
    public Optional<ShopMenuCollectionImage> findById(ShopMenuCollectionImageId id) {
        return shopMenuCollectionImageJpaRepository.findById(id.value())
            .map(ShopMenuCollectionImageMapper::toDomain);
    }

    @Override
    public List<ShopMenuCollectionImage> findAllByShopId(ShopId shopId) {
        return shopMenuCollectionImageJpaRepository.findAllByShopIdOrderBySortAsc(shopId.value()).stream()
            .map(ShopMenuCollectionImageMapper::toDomain)
            .toList();
    }

    @Override
    public void delete(ShopMenuCollectionImage image) {
        shopMenuCollectionImageJpaRepository.deleteById(image.getId());
    }
}
