package com.tastyhouse.core.domain.product.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.vo.ProductId;

/**
 * 상품 write 포트.
 *
 * <p>표현 목적 조회(목록·검색·상세 투영)는 infrastructure-module의 {@code ProductQueryDao}가 담당하고,
 * 이 포트에는 command 경로·도메인 서비스가 불변식 검증과 상태 전이를 위해 쓰는 단건 로드·저장만 남긴다.
 */
public interface ProductRepository {

    Optional<Product> findById(ProductId id);

    Product save(Product product);
}
