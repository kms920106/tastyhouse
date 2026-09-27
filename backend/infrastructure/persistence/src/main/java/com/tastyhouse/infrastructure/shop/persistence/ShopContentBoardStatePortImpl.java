package com.tastyhouse.infrastructure.shop.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopContentBoardState;
import com.tastyhouse.application.shop.port.out.write.ShopContentBoardStatePort;

@Repository
public class ShopContentBoardStatePortImpl implements ShopContentBoardStatePort {
    private final ShopContentBoardJpaRepository shopContentBoardJpaRepository;

    public ShopContentBoardStatePortImpl(ShopContentBoardJpaRepository shopContentBoardJpaRepository) {
        this.shopContentBoardJpaRepository = shopContentBoardJpaRepository;
    }

    @Override
    public ShopContentBoardState save(ShopContentBoardState shopContentBoard) {
        if (shopContentBoard.id() == null) {
            ShopContentBoardJpaEntity saved = shopContentBoardJpaRepository.save(ShopContentBoardMapper.toEntity(shopContentBoard));
            return ShopContentBoardMapper.toState(saved);
        }

        ShopContentBoardJpaEntity entity = shopContentBoardJpaRepository.findById(shopContentBoard.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 콘텐츠보드입니다: " + shopContentBoard.id()));
        ShopContentBoardMapper.applyChanges(entity, shopContentBoard);
        return ShopContentBoardMapper.toState(entity);
    }

    @Override
    public Optional<ShopContentBoardState> findById(Long id) {
        return shopContentBoardJpaRepository.findById(id).map(ShopContentBoardMapper::toState);
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
