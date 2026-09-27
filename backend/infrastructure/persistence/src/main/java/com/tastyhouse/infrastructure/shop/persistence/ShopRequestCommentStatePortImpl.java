package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.shop.port.out.write.ShopRequestCommentState;
import com.tastyhouse.application.shop.port.out.write.ShopRequestCommentStatePort;

@Repository
public class ShopRequestCommentStatePortImpl implements ShopRequestCommentStatePort {
    private final ShopRequestCommentJpaRepository shopRequestCommentJpaRepository;

    public ShopRequestCommentStatePortImpl(ShopRequestCommentJpaRepository shopRequestCommentJpaRepository) {
        this.shopRequestCommentJpaRepository = shopRequestCommentJpaRepository;
    }

    @Override
    public ShopRequestCommentState save(ShopRequestCommentState shopRequestComment) {
        ShopRequestCommentJpaEntity saved =
            shopRequestCommentJpaRepository.save(ShopRequestCommentMapper.toEntity(shopRequestComment));
        return ShopRequestCommentMapper.toState(saved);
    }
}
