package com.tastyhouse.application.shop.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.shop.port.out.write.ShopMenuCollectionImageStatePort;
import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;

public class ShopMenuCollectionImageStore implements ShopMenuCollectionImageRepository {
    private final ShopMenuCollectionImageStatePort shopMenuCollectionImageStatePort;

    public ShopMenuCollectionImageStore(ShopMenuCollectionImageStatePort shopMenuCollectionImageStatePort) {
        this.shopMenuCollectionImageStatePort = shopMenuCollectionImageStatePort;
    }

    @Override
    public ShopMenuCollectionImage save(ShopMenuCollectionImage image) {
        return ShopMenuCollectionImageStateMapper.toDomain(shopMenuCollectionImageStatePort.save(ShopMenuCollectionImageStateMapper.toState(image)));
    }

    @Override
    public Optional<ShopMenuCollectionImage> findById(ShopMenuCollectionImageId id) {
        return shopMenuCollectionImageStatePort.findById(id.value()).map(ShopMenuCollectionImageStateMapper::toDomain);
    }

    @Override
    public List<ShopMenuCollectionImage> findAllByShopId(ShopId shopId) {
        return shopMenuCollectionImageStatePort.findAllByShopId(shopId.value()).stream()
            .map(ShopMenuCollectionImageStateMapper::toDomain)
            .toList();
    }

    @Override
    public void delete(ShopMenuCollectionImage image) {
        shopMenuCollectionImageStatePort.delete(ShopMenuCollectionImageStateMapper.toState(image));
    }
}
