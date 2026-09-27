package com.tastyhouse.application.shop.store;

import java.util.Optional;

import com.tastyhouse.application.shop.port.out.write.ShopContentBoardStatePort;
import com.tastyhouse.domain.shop.model.ShopContentBoard;

public class ShopContentBoardStore implements ShopContentBoardRepository {
    private final ShopContentBoardStatePort shopContentBoardStatePort;

    public ShopContentBoardStore(ShopContentBoardStatePort shopContentBoardStatePort) {
        this.shopContentBoardStatePort = shopContentBoardStatePort;
    }

    @Override
    public ShopContentBoard save(ShopContentBoard shopContentBoard) {
        return ShopContentBoardStateMapper.toDomain(shopContentBoardStatePort.save(ShopContentBoardStateMapper.toState(shopContentBoard)));
    }

    @Override
    public Optional<ShopContentBoard> findById(Long id) {
        return shopContentBoardStatePort.findById(id).map(ShopContentBoardStateMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        shopContentBoardStatePort.deleteById(id);
    }

    @Override
    public long countByShopId(Long shopId) {
        return shopContentBoardStatePort.countByShopId(shopId);
    }
}
