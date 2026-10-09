package com.tastyhouse.infrastructure.jpa.shop.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopRequestComment;
import com.tastyhouse.application.shop.port.out.write.ShopRequestCommentSavePort;

@Repository
class ShopRequestCommentPersistenceAdapter implements ShopRequestCommentSavePort {

    private final ShopRequestCommentJpaRepository shopRequestCommentJpaRepository;

    public ShopRequestCommentPersistenceAdapter(ShopRequestCommentJpaRepository shopRequestCommentJpaRepository) {
        this.shopRequestCommentJpaRepository = shopRequestCommentJpaRepository;
    }

    @Override
    public ShopRequestComment save(ShopRequestComment shopRequestComment) {
        ShopRequestCommentJpaEntity saved =
            shopRequestCommentJpaRepository.save(ShopRequestCommentMapper.toEntity(shopRequestComment));
        return ShopRequestCommentMapper.toDomain(saved);
    }
}
