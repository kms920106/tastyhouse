package com.tastyhouse.infrastructure.shop.persistence;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shop.model.ShopRequestComment;
import com.tastyhouse.domain.shop.repository.ShopRequestCommentRepository;

/**
 * 요청건 문의 댓글 write 어댑터.
 *
 * <p>append-only라 insert 경로만 있다 — 다른 어댑터의 {@code save}가 갖는 update 분기가 필요 없다
 * ({@code ShopChangeHistoryRepositoryImpl}과 같은 형태).
 */
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
