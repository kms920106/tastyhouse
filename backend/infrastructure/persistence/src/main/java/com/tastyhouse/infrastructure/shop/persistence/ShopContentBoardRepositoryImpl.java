package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.domain.shop.repository.ShopContentBoardRepository;

@Repository
public class ShopContentBoardRepositoryImpl implements ShopContentBoardRepository {
    private final ShopContentBoardJpaRepository shopContentBoardJpaRepository;

    public ShopContentBoardRepositoryImpl(ShopContentBoardJpaRepository shopContentBoardJpaRepository) {
        this.shopContentBoardJpaRepository = shopContentBoardJpaRepository;
    }

    @Override
    public ShopContentBoard save(ShopContentBoard shopContentBoard) {
        if (shopContentBoard.getId() == null) {
            ShopContentBoardJpaEntity saved = shopContentBoardJpaRepository.save(ShopContentBoardMapper.toEntity(shopContentBoard));
            return ShopContentBoardMapper.toDomain(saved);
        }

        ShopContentBoardJpaEntity entity = shopContentBoardJpaRepository.findById(shopContentBoard.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 콘텐츠보드입니다: " + shopContentBoard.getId()));
        ShopContentBoardMapper.applyChanges(entity, shopContentBoard);
        return ShopContentBoardMapper.toDomain(entity);
    }

    @Override
    public Optional<ShopContentBoard> findById(Long id) {
        return shopContentBoardJpaRepository.findById(id).map(ShopContentBoardMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        shopContentBoardJpaRepository.deleteById(id);
    }

    @Override
    public long countByShopId(Long shopId) {
        return shopContentBoardJpaRepository.countByShopId(shopId);
    }
}
