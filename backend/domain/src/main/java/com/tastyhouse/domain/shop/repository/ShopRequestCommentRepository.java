package com.tastyhouse.domain.shop.repository;

import com.tastyhouse.domain.shop.model.ShopRequestComment;

/**
 * 요청건 문의 댓글 write 포트.
 *
 * <p>append-only라 저장만 필요하다. 스레드 조회는 CQRS query 측
 * {@code ShopRequestQueryDao#findComments}가 담당하므로 이 포트에 두지 않는다.
 */
public interface ShopRequestCommentRepository {

    ShopRequestComment save(ShopRequestComment shopRequestComment);
}
