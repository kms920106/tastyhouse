package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopRequestComment;
import com.tastyhouse.domain.shop.repository.ShopRequestCommentRepository;

@Repository
public class ShopRequestCommentRepositoryImpl implements ShopRequestCommentRepository {
    private final ShopRequestCommentJpaRepository shopRequestCommentJpaRepository;

    public ShopRequestCommentRepositoryImpl(ShopRequestCommentJpaRepository shopRequestCommentJpaRepository) {
        this.shopRequestCommentJpaRepository = shopRequestCommentJpaRepository;
    }

    @Override
    public ShopRequestComment save(ShopRequestComment shopRequestComment) {
        ShopRequestCommentJpaEntity saved =
            shopRequestCommentJpaRepository.save(ShopRequestCommentMapper.toEntity(shopRequestComment));
        return ShopRequestCommentMapper.toDomain(saved);
    }
}
